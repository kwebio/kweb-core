package kweb

import com.github.salomonbrys.kotson.fromJson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.cio.websocket.Frame.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kweb.client.*
import kweb.client.ClientConnection.Caching
import kweb.html.HtmlDocumentSupplier
import kweb.plugins.KwebPlugin
import kweb.util.*
import kweb.util.NotFoundException
import mu.KotlinLogging
import org.jsoup.nodes.DataNode
import java.io.Closeable
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.abs

private val MAX_PAGE_BUILD_TIME: Duration = Duration.ofSeconds(5)
private val CLIENT_STATE_TIMEOUT: Duration = Duration.ofHours(48)

private val logger = KotlinLogging.logger {}

class Kweb private constructor(
        val debug: Boolean,
        val plugins: List<KwebPlugin>
) : Closeable {

    /**
     *
     * The core kwebserver, and the starting point for almost any Kweb app.  This will element a HTTP server and respond
     * with a javascript page which will establish a websocket connection to retrieveJs and send instructions and data
     * between browser and server.
     *
     * @property port  The TCP port on which the HTTP server should listen
     * @property debug Should be set to true during development as it will provide useful warnings and other feedback,
     *                 but false during production because it is inefficient at scale
     * @property plugins A list of Kweb plugins to be loaded by Kweb
     * @property buildPage A lambda which will build the webpage to be served to the user, this is where your code should
     *                     go
     */
    constructor(
            port: Int,
            debug: Boolean = true,
            plugins: List<KwebPlugin> = Collections.emptyList(),
            httpsConfig: EngineSSLConnectorConfig? = null,
            buildPage: WebBrowser.() -> Unit
    ) : this(debug, plugins) {
        logger.info("Initializing Kweb listening on port $port")

        if (debug) {
            logger.warn("Debug mode enabled, if in production use KWeb(debug = false)")
        }

        server = createServer(port, httpsConfig, buildPage)

        server!!.start()
        logger.info { "KWeb is listening on port $port" }
    }

    /**
     * If you have an existing Ktor server, you can use the Kweb class as a feature. Adding this to your code is easy:
     *
     * ```
     *     install(DefaultHeaders)
     *     install(Compression)
     *     install(WebSockets) {
     *         pingPeriod = Duration.ofSeconds(10)
     *         timeout = Duration.ofSeconds(30)
     *     }
     *
     *     install(Kweb) {
     *         // Set debug or plugins here, if you'd like
     *         buildPage = {
     *             // Your app goes here as it would using the Ktor constructor
     *         }
     *     }
     * ```
     *
     * @see kweb.demos.feature.kwebFeature for an example
     */
    companion object Feature : ApplicationFeature<Application, Feature.Configuration, Kweb> {
        class Configuration {
            var debug: Boolean = true
            var plugins: List<KwebPlugin> = Collections.emptyList()
            @Deprecated("Please use the Ktor syntax for defining page handlers instead: $buildPageReplacementCode")
            var buildPage: (WebBrowser.() -> Unit)? = null
        }

        override val key = AttributeKey<Kweb>("Kweb")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): Kweb {
            val configuration = Configuration().apply(configure)
            val feature = Kweb(configuration.debug, configuration.plugins)

            configuration.buildPage?.let {
                logger.info { "Initializing Kweb with deprecated buildPage, this functionality will be removed in a future version" }
                pipeline.installKwebOnRemainingRoutes(it)
            }
            feature.installRequiredKwebComponents(pipeline)

            return feature
        }
    }

    private val clientState: ConcurrentHashMap<String, RemoteClientState> = ConcurrentHashMap()

    private var server: JettyApplicationEngine? = null

    fun isCatchingOutbound() = outboundMessageCatcher.get() != null

    fun isNotCatchingOutbound() = !isCatchingOutbound()

    fun catchOutbound(f: () -> Unit): List<String> {
        require(outboundMessageCatcher.get() == null) { "Can't nest withThreadLocalOutboundMessageCatcher()" }

        val jsList = ArrayList<String>()
        outboundMessageCatcher.set(jsList)
        f()
        outboundMessageCatcher.set(null)
        return jsList
    }

    fun callJs(clientId: String, javascript: String, parameters: String, args: List<Any?>,
               jsCached: Boolean = false, cacheId: Int? = null) {
        val wsClientData = clientState[clientId] ?: error("Client id $clientId not found")
        wsClientData.lastModified = Instant.now()
        val debugToken: String? = if(!debug) null else {
            val dt = abs(random.nextLong()).toString(16)
            wsClientData.debugTokens[dt] = DebugInfo(javascript, "executing", Throwable())
            dt
        }
        val outboundMessageCatcher = outboundMessageCatcher.get()
        if (outboundMessageCatcher == null) {
            if (jsCached) { //if the js is already cached we do not need to send the js string or the js function parameters.
                wsClientData.send(Server2ClientMessage(yourId = clientId, debugToken, jsId = cacheId, arguments = args))
            } else {
                wsClientData.send(Server2ClientMessage(yourId = clientId, debugToken, js = javascript,
                        parameters = parameters, jsId = cacheId, arguments = args))
            }
        } else {
            logger.debug("Temporarily storing message for $clientId in threadlocal outboundMessageCatcher")
            outboundMessageCatcher.add(javascript)
        }
    }

    fun callJsWithCallback(clientId: String, javascript: String, parameters: String, args: List<Any?>,
                           jsCached: Boolean = false, cacheId: Int? = null,
                           callbackId: Int, callback: (Any?) -> Unit) {
        val wsClientData = clientState[clientId] ?: error("Client id $clientId not found")
        wsClientData.lastModified = Instant.now()
        val debugToken: String? = if(!debug) null else {
            val dt = abs(random.nextLong()).toString(16)
            wsClientData.debugTokens[dt] = DebugInfo(javascript, "executing", Throwable())
            dt
        }
        wsClientData.handlers[callbackId] = callback
        if (jsCached) { //if the js is already cached we do not need to send the js string or the js function parameters.
            wsClientData.send(Server2ClientMessage(yourId = clientId, debugToken, jsId = cacheId, arguments = args,
                    callbackId = callbackId))
        } else {
            wsClientData.send(Server2ClientMessage(yourId = clientId, debugToken, js = javascript,
                    parameters = parameters, jsId = cacheId, arguments = args, callbackId = callbackId))
        }
    }

    fun execute(clientId: String, javascript: String) {
        val wsClientData = clientState.get(clientId) ?: error("Client id $clientId not found")
        wsClientData.lastModified = Instant.now()
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(javascript, "executing", Throwable()))
            dt
        }
        val outboundMessageCatcher = outboundMessageCatcher.get()
        if (outboundMessageCatcher == null) {
            wsClientData.send(Server2ClientMessage(yourId = clientId, debugToken = debugToken, js = javascript))
        } else {
            logger.debug("Temporarily storing message for $clientId in threadlocal outboundMessageCatcher")
            outboundMessageCatcher.add(javascript)
        }
    }

    fun cacheAndExecute(clientId: String, cacheId: Int, js: String, parameters: String, args: List<Any?>) {
        val wsClientData = clientState[clientId] ?: error("Client id $clientId not found")
        wsClientData.lastModified = Instant.now()
        val outboundMessageCatcher = outboundMessageCatcher.get()
        if (outboundMessageCatcher == null) {
            wsClientData.send(Server2ClientMessage(yourId = clientId, debugToken = null, js = js,
                    jsId = cacheId, parameters = parameters, arguments = args))
        }

    }

    fun executeFromCache(clientId: String, cacheId: Int, args: List<Any?>) {
        val wsClientData = clientState[clientId] ?: error("Client id $clientId not found")
        wsClientData.lastModified = Instant.now()
        val outboundMessageCatcher = outboundMessageCatcher.get()
        if (outboundMessageCatcher == null) {
            wsClientData.send(Server2ClientMessage(yourId = clientId, debugToken = null,
                    jsId = cacheId, arguments = args))
        }
    }

    fun executeWithCallback(clientId: String, javascript: String, callbackId: Int, handler: (Any) -> Unit) {
        // TODO: Should return handle which can be used for cleanup of event listeners
        val wsClientData = clientState.get(clientId) ?: error("Client id $clientId not found")
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(javascript, "executing with callback", Throwable()))
            dt
        }
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(Server2ClientMessage(yourId = clientId, debugToken = debugToken, js = javascript))
    }

    fun removeCallback(clientId: String, callbackId: Int) {
        clientState[clientId]?.handlers?.remove(callbackId)
    }

    fun evaluate(clientId: String, expression: String, handler: (Any) -> Unit) {
        val wsClientData = clientState.get(clientId)
                ?: error("Failed to evaluate JavaScript because client id $clientId not found")
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(expression, "evaluating", Throwable()))
            dt
        }
        val callbackId = Math.abs(random.nextInt())
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(Server2ClientMessage(yourId = clientId, callbackId = callbackId, js = expression, debugToken = debugToken))
    }

    override fun close() {
        logger.info("Shutting down Kweb")
        server?.stop(0, 0)
    }

    private fun createServer(port: Int, httpsConfig: EngineSSLConnectorConfig?, buildPage: WebBrowser.() -> Unit): JettyApplicationEngine {
        return embeddedServer(Jetty, applicationEngineEnvironment {
            this.module {
                install(DefaultHeaders)
                install(Compression)
                install(WebSockets) {
                    pingPeriod = Duration.ofSeconds(10)
                    timeout = Duration.ofSeconds(30)
                }

                setupKweb(this, buildPage)
            }

            connector {
                this.port = port
                this.host = "0.0.0.0"
            }

            if (httpsConfig != null)
                connectors.add(httpsConfig)
        })
    }

    private fun setupKweb(application: Application, buildPage: WebBrowser.() -> Unit) {

        application.routing {

            get("/robots.txt") {
                call.response.status(HttpStatusCode.NotFound)
                call.respondText("robots.txt not currently supported by kweb")
            }

            get("/favicon.ico") {
                call.response.status(HttpStatusCode.NotFound)
                call.respondText("favicons not currently supported by kweb")
            }

            get("/{visitedUrl...}") {
                respondKweb(call, buildPage)
            }

        }

        installRequiredKwebComponents(application)
    }

    // We can't convert this param to receiver because it's called on receiver in the companion Feature
    private fun installRequiredKwebComponents(application: Application) {
        HtmlDocumentSupplier.createDocTemplate(plugins, application.routing {  })

        application.routing {
            webSocket("/ws") {
                listenForWebsocketConnection()
            }
        }

        GlobalScope.launch {
            while (true) {
                delay(Duration.ofMinutes(1))
                cleanUpOldClientStates()
            }
        }
    }

    private suspend fun DefaultWebSocketSession.listenForWebsocketConnection() {
        val hello = gson.fromJson<Client2ServerMessage>((incoming.receive() as Text).readText())

        if (hello.hello == null) {
            error("First message from client isn't 'hello'")
        }

        val remoteClientState = clientState.get(hello.id)
                ?: error("Unable to find server state corresponding to client id ${hello.id}")

        assert(remoteClientState.clientConnection is Caching)
        logger.debug { "Received message from remoteClient ${remoteClientState.id}, flushing outbound message cache" }
        val cachedConnection = remoteClientState.clientConnection as Caching
        val webSocketClientConnection = ClientConnection.WebSocket(this)
        remoteClientState.clientConnection = webSocketClientConnection
        logger.debug { "Set clientConnection for ${remoteClientState.id} to WebSocket, sending ${cachedConnection.size} cached messages" }
        cachedConnection.read().forEach { webSocketClientConnection.send(it) }


        try {
            for (frame in incoming) {
                try {
                    logger.debug { "Message received from client" }

                    if (frame is Text) {
                        val message = gson.fromJson<Client2ServerMessage>(frame.readText())
                        logger.debug { "Message received: $message" }
                        if (message.error != null) {
                            handleError(message.error, remoteClientState)
                        } else {
                            when {
                                message.callback != null -> {
                                    val (resultId, result) = message.callback
                                    val resultHandler = remoteClientState.handlers[resultId]
                                            ?: error("No data handler for $resultId for client ${remoteClientState.id}")
                                    resultHandler(result ?: "")
                                }
                                message.historyStateChange != null -> {

                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Exception while receiving websocket message", e)
                }
            }
        } finally {
            logger.info("WS session disconnected for client id: ${remoteClientState.id}")
            remoteClientState.clientConnection = Caching()
        }
    }

    suspend fun respondKweb(call: ApplicationCall, buildPage: WebBrowser.() -> Unit) {
        val htmlDocument = HtmlDocumentSupplier.getTemplateCopy()

        val kwebSessionId = createNonce()

        val remoteClientState = clientState.getOrPut(kwebSessionId) {
            RemoteClientState(id = kwebSessionId, clientConnection = Caching())
        }

        val httpRequestInfo = HttpRequestInfo(call.request)

        try {
            val webBrowser = WebBrowser(kwebSessionId, httpRequestInfo, this)
            webBrowser.htmlDocument.set(htmlDocument)
            if (debug) {
                warnIfBlocking(maxTimeMs = MAX_PAGE_BUILD_TIME.toMillis(), onBlock = { thread ->
                    logger.warn { "buildPage lambda must return immediately but has taken > $MAX_PAGE_BUILD_TIME.  More info at DEBUG loglevel" }

                    val logStatementBuilder = StringBuilder()
                    logStatementBuilder.appendln("buildPage lambda must return immediately but has taken > $MAX_PAGE_BUILD_TIME, appears to be blocking here:")

                    thread.stackTrace.pruneAndDumpStackTo(logStatementBuilder)
                    val logStatement = logStatementBuilder.toString()
                    logger.debug { logStatement }
                }) {
                    try {
                        buildPage(webBrowser)
                    } catch (e: Exception) {
                        logger.error("Exception thrown building page", e)
                    }
                    logger.debug { "Outbound message queue size after buildPage is ${(remoteClientState.clientConnection as Caching).queueSize()}" }
                }
            } else {
                try {
                    buildPage(webBrowser)
                } catch (e: Exception) {
                    logger.error("Exception thrown building page", e)
                }
                logger.debug { "Outbound message queue size after buildPage is ${(remoteClientState.clientConnection as Caching).queueSize()}" }
            }
            for (plugin in plugins) {
                execute(kwebSessionId, plugin.executeAfterPageCreation())
            }

            webBrowser.htmlDocument.set(null) // Don't think this webBrowser will be used again, but not going to risk it

            val initialCachedMessages = remoteClientState.clientConnection as Caching

            remoteClientState.clientConnection = Caching()

            val bootstrapJS = BootstrapJs.hydrate(
                    kwebSessionId,
                    initialCachedMessages
                            .read().joinToString(separator = "\n") { "handleInboundMessage($it);" })

            htmlDocument.head().appendElement("script")
                    .attr("language", "JavaScript")
                    .appendChild(DataNode(bootstrapJS))
            htmlDocument.outputSettings().prettyPrint(debug)


            call.respondText(htmlDocument.outerHtml(), ContentType.Text.Html)
        } catch (nfe: NotFoundException) {
            call.response.status(HttpStatusCode.NotFound)
            call.respondText("URL ${call.request.uri} not found.", ContentType.parse("text/plain"))
        } catch (e: Exception) {
            val logToken = random.nextLong().toString(16)

            logger.error(e) { "Exception thrown while rendering page, code $logToken" }

            call.response.status(HttpStatusCode.InternalServerError)
            call.respondText("""
                        Internal Server Error.

                        Please include code $logToken in any error report to help us track it down.
""".trimIndent())
        }
    }

    private fun handleError(error: Client2ServerMessage.ErrorMessage, remoteClientState: RemoteClientState) {
        val debugInfo = remoteClientState.debugTokens[error.debugToken]
                ?: error("DebugInfo message not found")
        val logStatementBuilder = StringBuilder()
        logStatementBuilder.appendln("JavaScript message: '${error.error.message}'")
        logStatementBuilder.appendln("Caused by ${debugInfo.action}: '${debugInfo.js}':")
        // TODO: Filtering the stacktrace like this seems a bit kludgy, although I can't think
        // TODO: of a specific reason why it would be bad.
        debugInfo.throwable.stackTrace.pruneAndDumpStackTo(logStatementBuilder)
        logger.error(logStatementBuilder.toString())
    }

    /**
     * Forces all currently connected clients to refresh their pages.  This can be useful if there has been a
     * code-change, for example.
     *
     * If there are a large number of connected clients this could place a lot of load on the server.  An
     * unexpected page refresh may also confuse website visitors.
     */
    fun refreshAllPages() = GlobalScope.launch(Dispatchers.Default) {
        for (client in clientState.values) {
            val message = Server2ClientMessage(
                    yourId = client.id,
                    js = "window.location.reload(true);",
                    debugToken = null)
            client.clientConnection.send(message.toJson())
        }
    }

    /**
     * Allow us to catch outbound messages temporarily and only for this thread.  This is used for immediate
     * execution of event handlers, see `Element.immediatelyOn`
     */
    private val outboundMessageCatcher: ThreadLocal<MutableList<String>?> = ThreadLocal.withInitial { null }

    private fun cleanUpOldClientStates() {
        val now = Instant.now()
        val toRemove = clientState.entries.mapNotNull { (id: String, state: RemoteClientState) ->
            if (Duration.between(state.lastModified, now) > CLIENT_STATE_TIMEOUT) {
                id
            } else {
                null
            }
        }
        if (toRemove.isNotEmpty()) {
            logger.info("Cleaning up client states for ids: $toRemove")
        }
        for (id in toRemove) {
            clientState.remove(id)
        }
    }

}

data class DebugInfo(val js: String, val action: String, val throwable: Throwable)

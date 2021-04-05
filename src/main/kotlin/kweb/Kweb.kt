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

    /**
     * Are outbound messages being cached for this thread (for example, because we're inside an immediateEvent callback block)?
     */
    fun isCatchingOutbound() = outboundMessageCatcher.get() != null

    /**
     * Execute a block of code in which any JavaScript sent to the browser during the execution of the block will be stored
     * and returned by this function.
     *
     * The main use-case is recording changes made to the DOM within an onImmediate event callback so that these can be
     * replayed in the browser when an event is triggered without a server round-trip.
     */
    fun catchOutbound(f: () -> Unit): List<JsFunction> {
        require(outboundMessageCatcher.get() == null) { "Can't nest withThreadLocalOutboundMessageCatcher()" }

        val jsList = ArrayList<JsFunction>()
        outboundMessageCatcher.set(jsList)
        f()
        outboundMessageCatcher.set(null)
        return jsList
    }

    /*TODO
    I think callJs and callJsWithCallback are simplified a good bit by moving the message creation to WebBrowser.callJs()
    There is a bit of duplication here, because we have to pass in the javascript string separately.
    We need it to create the debugToken. Some server2ClientMessages will contain the javascript,
     but messages that call cached functions will not have it. So we have to make sure we pass it in separately.*/
    fun callJs(server2ClientMessage: Server2ClientMessage, javascript: String) {
        val wsClientData = clientState[server2ClientMessage.yourId]
                ?: error("Client id ${server2ClientMessage.yourId} not found")
        wsClientData.lastModified = Instant.now()
        val debugToken: String? = if(!debug) null else {
            val dt = abs(random.nextLong()).toString(16)
            wsClientData.debugTokens[dt] = DebugInfo(javascript, "executing", Throwable())
            dt
        }
        server2ClientMessage.debugToken = debugToken
        val outboundMessageCatcher = outboundMessageCatcher.get()
        if (outboundMessageCatcher == null) {
            wsClientData.send(server2ClientMessage)
        } else {
            logger.debug("Temporarily storing message for ${server2ClientMessage.yourId} in threadlocal outboundMessageCatcher")
            val jsFunction = JsFunction(server2ClientMessage.jsId!!, server2ClientMessage.arguments!!)
            outboundMessageCatcher.add(jsFunction)
            //If we have an outboundMessageCatcher, we do not want to execute the jsCode in this message.
            //We still need to send the Server2ClientMessage, to cache the jsCode.
            //So, we take the message, and set arguments to null, so the server knows not to run this code.
            server2ClientMessage.arguments = null
            wsClientData.send(server2ClientMessage)
        }
    }

    fun callJsWithCallback(server2ClientMessage: Server2ClientMessage,
                           javascript: String, callback: (Any) -> Unit) {
        //TODO I could use some help improving this error message
        require(outboundMessageCatcher.get() == null) { "Can not use callback while page is rendering" }
        val wsClientData = clientState[server2ClientMessage.yourId]
                ?: error("Client id ${server2ClientMessage.yourId} not found")
        wsClientData.lastModified = Instant.now()
        val debugToken: String? = if(!debug) null else {
            val dt = abs(random.nextLong()).toString(16)
            wsClientData.debugTokens[dt] = DebugInfo(javascript, "executing", Throwable())
            dt
        }
        server2ClientMessage.debugToken = debugToken
        wsClientData.handlers[server2ClientMessage.callbackId!!] = callback
        wsClientData.send(server2ClientMessage)
    }

    fun removeCallback(clientId: String, callbackId: Int) {
        clientState[clientId]?.handlers?.remove(callbackId)
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
                //this code block looks a little funny now, but I still think moving the message creation out of Kweb.callJs() was the right move
                val js = plugin.executeAfterPageCreation()
                callJs(Server2ClientMessage(yourId = kwebSessionId, js = js), javascript = js)
            }

            webBrowser.htmlDocument.set(null) // Don't think this webBrowser will be used again, but not going to risk it

            val initialCachedMessages = remoteClientState.clientConnection as Caching

            remoteClientState.clientConnection = Caching()

            val initialMessages = initialCachedMessages.read()//the initialCachedMessages queue can only be read once

            val cachedFunctions = mutableListOf<String>()
            val cachedIds = mutableListOf<Int>()
            for (msg in initialMessages) {
                val deserialedMsg = gson.fromJson<Server2ClientMessage>(msg)

                //For some reason the final msg in initialMessages looks like this,
                //{"yourId":"gkUd4k","debugToken":"1446aab757c06931","js":""}
                //I'm not sure what the point of this message is, and I can't find what is sending it.
                //But, it causes problems if we try to add that to the cache, so we just make sure the jsId isn't null
                //This is a useful check anyway, because we do let Server2ClientMessages have null jsId's, and trying to add
                //a function from one of those messages wouldn't work.
                if (deserialedMsg.jsId != null) {
                    if (!cachedIds.contains(deserialedMsg.jsId)) {
                        val cachedFunction = """'${deserialedMsg.jsId}' : function(${deserialedMsg.parameters}) { ${deserialedMsg.js} }"""
                        cachedFunctions.add(cachedFunction)
                        cachedIds.add(deserialedMsg.jsId)
                    }

                }
            }

            val functionCacheString = "let cachedFunctions = { \n${cachedFunctions.joinToString(separator = ",\n")} };"

            val bootstrapJS = BootstrapJs.hydrate(
                    kwebSessionId,
                    initialMessages.joinToString(separator = "\n") { "handleInboundMessage($it);" },
                    functionCacheString
            )

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
    private val outboundMessageCatcher: ThreadLocal<MutableList<JsFunction>?> = ThreadLocal.withInitial { null }

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

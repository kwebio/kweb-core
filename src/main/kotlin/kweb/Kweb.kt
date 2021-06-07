package kweb

import com.google.common.cache.CacheBuilder
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kweb.client.*
import kweb.client.ClientConnection.Caching
import kweb.config.KwebConfiguration
import kweb.config.KwebDefaultConfiguration
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
import kotlin.math.abs

private val MAX_PAGE_BUILD_TIME: Duration = Duration.ofSeconds(5)
private val CLIENT_STATE_TIMEOUT: Duration = Duration.ofHours(48)

private val logger = KotlinLogging.logger {}

class Kweb private constructor(
        val debug: Boolean,
        val plugins: List<KwebPlugin>,
        val kwebConfig: KwebConfiguration,
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
            kwebConfig: KwebConfiguration = KwebDefaultConfiguration,
            buildPage: WebBrowser.() -> Unit,
    ) : this(
            debug = debug,
            plugins = plugins,
            kwebConfig = kwebConfig,
    ) {
        logger.info("Initializing Kweb listening on port $port")

        if (debug) {
            logger.warn("Debug mode enabled, if in production use KWeb(debug = false)")
        }

        kwebConfig.validate()

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
        // Note that this is not KwebConfiguration, which is a different thing
        class Configuration {
            var debug: Boolean = true
            var plugins: List<KwebPlugin> = Collections.emptyList()
            var kwebConfig: KwebConfiguration = KwebDefaultConfiguration
            @Deprecated("Please use the Ktor syntax for defining page handlers instead: $buildPageReplacementCode")
            var buildPage: (WebBrowser.() -> Unit)? = null
        }

        override val key = AttributeKey<Kweb>("Kweb")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): Kweb {
            val configuration = Configuration().apply(configure)
            configuration.kwebConfig.validate()
            val feature = Kweb(configuration.debug, configuration.plugins, configuration.kwebConfig)

            configuration.buildPage?.let {
                logger.info { "Initializing Kweb with deprecated buildPage, this functionality will be removed in a future version" }
                pipeline.installKwebOnRemainingRoutes(it)
            }
            feature.installRequiredKwebComponents(pipeline)

            return feature
        }
    }

    val clientState = CacheBuilder.newBuilder()
        .expireAfterAccess(kwebConfig.clientStateTimeout)
        .build<String, RemoteClientState>()

    //: ConcurrentHashMap<String, RemoteClientState> = ConcurrentHashMap()

    private var server: JettyApplicationEngine? = null

    /**
     * Are outbound messages being cached for this thread (for example, because we're inside an immediateEvent callback block)?
     */
    fun isCatchingOutbound() = outboundMessageCatcher.get()?.catcherType

    /**
     * Execute a block of code in which any JavaScript sent to the browser during the execution of the block will be stored
     * and returned by this function.
     *
     * The main use-case is recording changes made to the DOM within an onImmediate event callback so that these can be
     * replayed in the browser when an event is triggered without a server round-trip.
     */
    fun catchOutbound(catchingType: CatcherType, f: () -> Unit): List<FunctionCall> {
        require(outboundMessageCatcher.get() == null) { "Can't nest withThreadLocalOutboundMessageCatcher()" }

        val jsList = ArrayList<FunctionCall>()
        outboundMessageCatcher.set(OutboundMessageCatcher(catchingType, jsList))
        f()
        outboundMessageCatcher.set(null)
        return jsList
    }

    /*
    FunctionCalls that point to functions already in the cache will not contain the jsCode needed to create the debugToken
    So we have to pass it separately here.
     */
    fun callJs(sessionId: String, funcCall: FunctionCall, javascript: String) {
        val wsClientData = clientState.getIfPresent(sessionId)
                ?: error("Client id $sessionId not found")
        wsClientData.lastModified = Instant.now()
        val debugToken: String? = if(!debug) null else {
            val dt = abs(random.nextLong()).toString(16)
            wsClientData.debugTokens[dt] = DebugInfo(javascript, "executing", Throwable())
            dt
        }
        val outboundMessageCatcher = outboundMessageCatcher.get()
        //TODO to make debugToken and shouldExecute in Server2ClientMessage vals instead of vars I decided to
        //make new functionCall objects. I don't know if changing these vals to vars is worth the overhead of creating
        //a new object though. So we may want to revert this change.
        if (outboundMessageCatcher == null) {
            val jsFuncCall = FunctionCall(debugToken, funcCall)
            wsClientData.send(Server2ClientMessage(sessionId, jsFuncCall))
        } else {
            logger.debug("Temporarily storing message for $sessionId in threadlocal outboundMessageCatcher")
            outboundMessageCatcher.functionList.add(funcCall)
            //Setting `shouldExecute` to false tells the server not to add this jsFunction to the client's cache,
            //but to not actually run the code. This is used to pre-cache functions on initial page render.
            val jsFuncCall = FunctionCall(debugToken, shouldExecute = false, funcCall)
            wsClientData.send(Server2ClientMessage(sessionId, jsFuncCall))
        }
    }

    fun callJsWithCallback(sessionId: String, funcCall: FunctionCall,
                           javascript: String, callback: (JsonElement) -> Unit) {
        val wsClientData = clientState.getIfPresent(sessionId)
                ?: error("Client id $sessionId not found")
        wsClientData.lastModified = Instant.now()
        funcCall.callbackId?.let {
            wsClientData.handlers[it] = callback
        } ?: error("Javascript function callback wasn't given a callbackId")
        callJs(sessionId, funcCall, javascript)
    }

    fun removeCallback(clientId: String, callbackId: Int) {
        clientState.getIfPresent(clientId)?.handlers?.remove(callbackId)
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
    }

    private suspend fun DefaultWebSocketSession.listenForWebsocketConnection() {
        val hello = Json.decodeFromString<Client2ServerMessage>((incoming.receive() as Text).readText())

        if (hello.hello == null) {
            error("First message from client isn't 'hello'")
        }

        val remoteClientState = clientState.getIfPresent(hello.id)
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

                logger.debug { "WebSocket frame of type ${frame.frameType} received" }

                // Retrieve the clientState so that it doesn't expire, replace it if it
                // has expired.
                clientState.get(hello.id) { remoteClientState }

                try {
                    logger.debug { "Message received from client" }

                    if (frame is Text) {
                        val message = Json.decodeFromString<Client2ServerMessage>(frame.readText())

                        logger.debug { "Message received: $message" }
                        if (message.error != null) {
                            handleError(message.error, remoteClientState)
                        } else {
                            when {
                                message.callback != null -> {
                                    val (resultId, result) = message.callback
                                    val resultHandler = remoteClientState.handlers[resultId]
                                            ?: error("No data handler for $resultId for client ${remoteClientState.id}")
                                    resultHandler(result)
                                }
                                message.historyStateChange != null -> {

                                }
                                message.keepalive -> {
                                    logger.debug { "keepalive received from client ${hello.id}" }
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

        val remoteClientState = clientState.get(kwebSessionId) {
            RemoteClientState(id = kwebSessionId, clientConnection = Caching())
        }

        val httpRequestInfo = HttpRequestInfo(call.request)

        try {
            val webBrowser = WebBrowser(kwebSessionId, httpRequestInfo, this)
            webBrowser.htmlDocument.set(htmlDocument)
            if (debug) {
                warnIfBlocking(maxTimeMs = kwebConfig.buildpageTimeout.toMillis(), onBlock = { thread ->
                    logger.warn { "buildPage lambda must return immediately but has taken > ${kwebConfig.buildpageTimeout}.  More info at DEBUG loglevel" }

                    val logStatementBuilder = StringBuilder()
                    logStatementBuilder.appendln("buildPage lambda must return immediately but has taken > ${kwebConfig.buildpageTimeout}, appears to be blocking here:")

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
                //A plugin with an empty js string was breaking functionality.
                if (js != "") {
                    val pluginFunction = FunctionCall(js = js)
                    callJs(kwebSessionId, pluginFunction, js)
                }
            }

            webBrowser.htmlDocument.set(null) // Don't think this webBrowser will be used again, but not going to risk it

            val initialCachedMessages = remoteClientState.clientConnection as Caching

            remoteClientState.clientConnection = Caching()

            val initialMessages = initialCachedMessages.read()//the initialCachedMessages queue can only be read once

            val cachedFunctions = mutableListOf<String>()
            val cachedIds = mutableListOf<Int>()
            for (msg in initialMessages) {
                val deserialedMsg = Json.decodeFromString<Server2ClientMessage>(msg)

                //We have a special case where some functions do not have jsId's. Trying to add one of those to the cache would cause problems.
                for (funcCall in deserialedMsg.functionCalls) {
                    if (funcCall.jsId != null) {
                        if (!cachedIds.contains(funcCall.jsId)) {
                            val cachedFunction = """${funcCall.jsId} : function(${funcCall.parameters}) { ${funcCall.js} }"""
                            cachedFunctions.add(cachedFunction)
                            cachedIds.add(funcCall.jsId)
                        }

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
        for (client in clientState.asMap().values) {
            val refreshCall = FunctionCall(js = "window.location.reload(true);")
            val message = Server2ClientMessage(client.id, refreshCall)
            client.clientConnection.send(Json.encodeToString(message))
        }
    }

    //TODO I think some of these things could be renamed for clarity. I think it is understandable as is, but there is room for improvement
    enum class CatcherType {
        EVENT, IMMEDIATE_EVENT, RENDER
    }
    data class OutboundMessageCatcher(var catcherType: CatcherType, val functionList: MutableList<FunctionCall>)

    /**
     * Allow us to catch outbound messages temporarily and only for this thread.  This is used for immediate
     * execution of event handlers, see `Element.onImmediate`
     */
    val outboundMessageCatcher: ThreadLocal<OutboundMessageCatcher?> = ThreadLocal.withInitial { null }

}

data class DebugInfo(val js: String, val action: String, val throwable: Throwable)

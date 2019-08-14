package io.kweb

import com.github.salomonbrys.kotson.fromJson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.Frame.Text
import io.ktor.http.cio.websocket.readText
import io.ktor.request.uri
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.*
import io.ktor.websocket.*
import io.kweb.browserConnection.KwebClientConnection
import io.kweb.browserConnection.KwebClientConnection.Caching
import io.kweb.client.*
import io.kweb.client.Server2ClientMessage.Instruction
import io.kweb.plugins.KwebPlugin
import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import org.apache.commons.io.IOUtils
import java.io.Closeable
import java.time.*
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList

private val MAX_PAGE_BUILD_TIME : Duration = Duration.ofSeconds(5)
private val CLIENT_STATE_TIMEOUT : Duration = Duration.ofHours(1)

/**
 * The core kwebserver, and the starting point for almost any Kweb app.  This will element a HTTP server and respond
 * with a javascript page which will establish a websocket connection to retrieveJs and send instructions and data
 * between browser and server.
 *
 * @property port  The TCP port on which the HTTP server should listen
 * @property debug Should be set to true during development as it will provide useful warnings and other feedback,
 *                 but false during production because it is inefficient at scale
 * @property refreshPageOnHotswap Detects code-reloads by [HotSwapAgent](http://hotswapagent.org/) and refreshes
 *                                any connected webpage if this is detected
 * @property plugins A list of Kweb plugins to be loaded by Kweb
 * @property onError A handler for JavaScript errors (only detected if `debug == true`)
 * @property maxPageBuildTimeMS If `debug == true` this is the maximum time permitted to build a page before a
 *                              warning is logged
 * @property buildPage A lambda which will build the webpage to be served to the user, this is where your code should
 *                     go
 */
class Kweb constructor(val port: Int,
                                  val debug: Boolean = true,
                                  val plugins: List<KwebPlugin> = Collections.emptyList(),
                                  val buildPage: WebBrowser.() -> Unit
) : Closeable {

    private val clientState: ConcurrentHashMap<String, RemoteClientState> = ConcurrentHashMap()


    private val mutableAppliedPlugins: MutableSet<KwebPlugin> = HashSet()
    val appliedPlugins: Set<KwebPlugin> get() = mutableAppliedPlugins

    private val server: JettyApplicationEngine

    init {
        logger.info("Initializing Kweb listening on port $port")

        if (debug) {
            logger.warn("Debug mode enabled, if in production use KWeb(debug = false)")
        }


        server = embeddedServer(Jetty, port) {
            install(DefaultHeaders)
            install(Compression)
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(10)
                timeout = Duration.ofSeconds(30)
            }

            routing {

                val bootstrapHtmlTemplate = generateHTMLTemplate()

                get("/robots.txt") {
                    call.response.status(HttpStatusCode.NotFound)
                    call.respondText("robots.txt not currently supported by kweb")
                }

                get("/favicon.ico") {
                    call.response.status(HttpStatusCode.NotFound)
                    call.respondText("favicons not currently supported by kweb")
                }

                listenForHTTPConnection(bootstrapHtmlTemplate)

                listenForWebsocketConnection()
            }
        }

        server.start()
        logger.info { "KWeb is listening on port $port" }

        GlobalScope.launch {
            while(true) {
                delay(Duration.ofMinutes(1))
                cleanUpOldClientStates()
            }
        }
    }

    private fun Routing.generateHTMLTemplate(): String {
        val startHeadBuilder = StringBuilder()
        val endHeadBuilder = StringBuilder()

        for (plugin in plugins) {
            applyPluginWithDependencies(plugin = plugin, appliedPlugins = mutableAppliedPlugins, endHeadBuilder = endHeadBuilder, startHeadBuilder = startHeadBuilder, routeHandler = this)
        }

        val resourceStream = Kweb::class.java.getResourceAsStream("kweb_bootstrap.html")
        val bootstrapHtmlTemplate = IOUtils.toString(resourceStream, Charsets.UTF_8)
                .replace("<!-- START HEADER PLACEHOLDER -->", startHeadBuilder.toString())
                .replace("<!-- END HEADER PLACEHOLDER -->", endHeadBuilder.toString())
        return bootstrapHtmlTemplate
    }

    private fun Routing.listenForWebsocketConnection(path : String = "/ws") {
        webSocket(path) {

            val hello = gson.fromJson<Client2ServerMessage>(((incoming.receive() as Text).readText()))

            if (hello.hello == null) {
                throw RuntimeException("First message from client isn't 'hello'")
            }

            val remoteClientState = clientState.get(hello.id)
                    ?: throw RuntimeException("Unable to find server state corresponding to client id ${hello.id}")

            assert(remoteClientState.clientConnection is Caching)
            logger.debug("Received message from remoteClient ${remoteClientState.id}, flushing outbound message cache")
            val oldConnection = remoteClientState.clientConnection as Caching
            val webSocketClientConnection = KwebClientConnection.WebSocket(this)
            remoteClientState.clientConnection = webSocketClientConnection
            logger.debug("Set clientConnection for ${remoteClientState.id} to WebSocket")
            oldConnection.read().forEach { webSocketClientConnection.send(it) }


            try {
                for (frame in incoming) {
                    try {
                        logger.debug { "Message received from client" }

                        if (frame is Text) {
                            val message = gson.fromJson<Client2ServerMessage>(frame.readText())
                            if (message.error != null) {
                                handleError(message.error, remoteClientState)
                            } else {
                                when {
                                    message.callback != null -> {
                                        val (resultId, result) = message.callback
                                        val resultHandler = remoteClientState.handlers[resultId]
                                                ?: throw RuntimeException("No data handler for $resultId for client ${remoteClientState.id}")
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
    }

    private fun Routing.listenForHTTPConnection(bootstrapHtmlTemplate: String) {
        get("/{visitedUrl...}") {
            val kwebSessionId = createNonce()

            val remoteClientState = clientState.getOrPut(kwebSessionId) {
                RemoteClientState(id = kwebSessionId, clientConnection = Caching())
            }

            val httpRequestInfo = io.kweb.client.HttpRequestInfo(call.request)

            try {
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
                            buildPage(WebBrowser(kwebSessionId, httpRequestInfo, this@Kweb))
                        } catch (e: Exception) {
                            logger.error("Exception thrown building page", e)
                        }
                        logger.debug { "Outbound message queue size after buildPage is ${(remoteClientState.clientConnection as Caching).queueSize()}" }
                    }
                } else {
                    try {
                        buildPage(WebBrowser(kwebSessionId, httpRequestInfo, this@Kweb))
                    } catch (e: Exception) {
                        logger.error("Exception thrown building page", e)
                    }
                    logger.debug { "Outbound message queue size after buildPage is ${(remoteClientState.clientConnection as Caching).queueSize()}" }
                }
                for (plugin in plugins) {
                    execute(kwebSessionId, plugin.executeAfterPageCreation())
                }

                val initialCachedMessages = remoteClientState.clientConnection as Caching

                remoteClientState.clientConnection = Caching()

                val bootstrapHtml = bootstrapHtmlTemplate
                        .replace("--CLIENT-ID-PLACEHOLDER--", kwebSessionId)
                        .replace("<!-- BUILD PAGE PAYLOAD PLACEHOLDER -->", initialCachedMessages.read().map { "handleInboundMessage($it);" }.joinToString(separator = "\n"))

                call.respondText(bootstrapHtml, ContentType.Text.Html)
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
    }

    private fun handleError(error: Client2ServerMessage.ErrorMessage, remoteClientState: RemoteClientState) {
        val debugInfo = remoteClientState.debugTokens[error.debugToken]
                ?: throw RuntimeException("DebugInfo message not found")
        val logStatementBuilder = StringBuilder()
        logStatementBuilder.appendln("JavaScript message: '${error.error.message}'")
        logStatementBuilder.appendln("Caused by ${debugInfo.action}: '${debugInfo.js}':")
        // TODO: Filtering the stacktrace like this seems a bit kludgy, although I can't think
        // TODO: of a specific reason why it would be bad.
        debugInfo.throwable.stackTrace.pruneAndDumpStackTo(logStatementBuilder)
        logger.error(logStatementBuilder.toString())
    }

    private fun applyPluginWithDependencies(plugin: KwebPlugin,
                                            appliedPlugins: MutableSet<KwebPlugin>,
                                            endHeadBuilder: java.lang.StringBuilder,
                                            startHeadBuilder: java.lang.StringBuilder,
                                            routeHandler: Routing) {
        for (dependantPlugin in plugin.dependsOn) {
            if (!appliedPlugins.contains(dependantPlugin)) {
                applyPluginWithDependencies(dependantPlugin, appliedPlugins, endHeadBuilder, startHeadBuilder, routeHandler)
                appliedPlugins.add(dependantPlugin)
            }
        }
        if (!appliedPlugins.contains(plugin)) {
            plugin.decorate(startHeadBuilder, endHeadBuilder)
            plugin.appServerConfigurator(routeHandler)
            appliedPlugins.add(plugin)
        }
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
                    execute = Server2ClientMessage.Execute("window.location.reload(true);"), debugToken = null)
            client.clientConnection.send(message.toJson())
        }
    }

    /**
     * Allow us to catch outbound messages temporarily and only for this thread.  This is used for immediate
     * execution of event handlers, see `Element.immediatelyOn`
     */
    private val outboundMessageCatcher: ThreadLocal<MutableList<String>?> = ThreadLocal.withInitial { null }

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

    fun execute(clientId: String, javascript: String) {
        val wsClientData = clientState.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        wsClientData.lastModified = Instant.now()
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(javascript, "executing", Throwable()))
            dt
        }
        val outboundMessageCatcher = outboundMessageCatcher.get()
        if (outboundMessageCatcher == null) {
            wsClientData.send(Server2ClientMessage(yourId = clientId, debugToken = debugToken, execute = Server2ClientMessage.Execute(javascript)))
        } else {
            logger.debug("Temporarily storing message for $clientId in threadloacal outboundMessageCatcher")
            outboundMessageCatcher.add(javascript)
        }
    }

    fun send(clientId: String, instruction: Instruction) = send(clientId, listOf(instruction))

    fun send(clientId: String, instructions: List<Instruction>) {
        if (outboundMessageCatcher.get() != null) {
            throw RuntimeException("""
                Can't send instruction because there is an outboundMessageCatcher.  You should check for this with
                """.trimIndent())
        }
        val wsClientData = clientState.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        wsClientData.lastModified = Instant.now()
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(instructions.toString(), "instructions", Throwable()))
            dt
        }
        wsClientData.send(Server2ClientMessage(yourId = clientId, instructions = instructions, debugToken = debugToken))
    }

    fun executeWithCallback(clientId: String, javascript: String, callbackId: Int, handler: (String) -> Unit) {
        // TODO: Should return handle which can be used for cleanup of event listeners
        val wsClientData = clientState.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(javascript, "executing with callback", Throwable()))
            dt
        }
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(Server2ClientMessage(yourId = clientId, debugToken = debugToken, execute = Server2ClientMessage.Execute(javascript)))
    }

    fun removeCallback(clientId: String, callbackId: Int) {
        clientState[clientId]?.handlers?.remove(callbackId)
    }

    fun evaluate(clientId: String, expression: String, handler: (String) -> Unit) {
        val wsClientData = clientState.get(clientId)
                ?: throw RuntimeException("Failed to evaluate JavaScript because client id $clientId not found")
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(expression, "evaluating", Throwable()))
            dt
        }
        val callbackId = Math.abs(random.nextInt())
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(Server2ClientMessage(yourId = clientId, evaluate = Server2ClientMessage.Evaluate(expression, callbackId), debugToken = debugToken))
    }

    override fun close() {
        logger.info("Shutting down Kweb")
        server.stop(0, 0, TimeUnit.SECONDS)
    }

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

package io.kweb

import com.github.salomonbrys.kotson.fromJson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.util.nextNonce
import io.ktor.websocket.*
import io.kweb.browserConnection.KwebClientConnection
import io.kweb.browserConnection.KwebClientConnection.Caching
import io.kweb.dev.hotswap.KwebHotswapPlugin
import io.kweb.plugins.KWebPlugin
import io.mola.galimatias.URL
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.apache.commons.io.IOUtils
import java.io.Closeable
import java.time.Duration
import java.util.*
import java.util.concurrent.*

/**
 * Created by ian on 12/31/16.
 */

typealias LogError = Boolean
typealias JavaScriptError = String

/**
 * The core kwebserver, and the starting point for almost any Kweb app.  This will element a HTTP server and respond
 * with a javascript page which will establish a websocket connection to retrieve and send instructions and data
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
class Kweb(val port: Int,
           val debug: Boolean = true,
           val refreshPageOnHotswap : Boolean = false,
           val plugins: List<io.kweb.plugins.KWebPlugin> = java.util.Collections.emptyList(),
           val appServerConfigurator: (io.ktor.routing.Routing) -> Unit = {},
           val onError : ((List<StackTraceElement>, io.kweb.JavaScriptError) -> io.kweb.LogError) = { _, _ ->  true},
           val maxPageBuildTimeMS : Long = 200,
           val buildPage: WebBrowser.() -> Unit
) : Closeable {
    companion object: mu.KLogging()

    // private val server: Any
    private val clients: ConcurrentHashMap<KwebSessionId, io.kweb.RemoteClientState> = java.util.concurrent.ConcurrentHashMap()
    private val mutableAppliedPlugins: MutableSet<io.kweb.plugins.KWebPlugin> = java.util.HashSet()
    val appliedPlugins: Set<io.kweb.plugins.KWebPlugin> get() = mutableAppliedPlugins

    private val server: NettyApplicationEngine

    init {

        //TODO: Need to do housekeeping to deleteIfExists old client data

        val startHeadBuilder = StringBuilder()
        val endHeadBuilder = StringBuilder()

        if (refreshPageOnHotswap) {
            KwebHotswapPlugin.addHotswapReloadListener({refreshAllPages()})
        }

        server = embeddedServer(Netty, port) {
            install(DefaultHeaders)
            install(CallLogging)
            install(WebSockets) {
                pingPeriod = Duration.ofMinutes(1)
            }
            install(Routing) {

                install(Sessions) {
                    cookie<KwebSessionId>("KWEB_SESSION_ID")
                }

                intercept(ApplicationCallPipeline.Infrastructure) {
                    if (call.sessions.get<KwebSessionId>() == null) {
                        logger.info {"Creating new session"}
                        call.sessions.set(KwebSessionId(nextNonce()))
                    }
                }

                // Register custom state.
                appServerConfigurator.invoke(this)

                // TODO this is pretty awful but don't see an alternative....
                // Register plugins and allow plugin specific routing to be added.
                for (plugin in plugins) {
                    applyPlugin(plugin = plugin, appliedPlugins = mutableAppliedPlugins, endHeadBuilder = endHeadBuilder, startHeadBuilder = startHeadBuilder, routeHandler = this)
                }

                val resourceStream = Kweb::class.java.getResourceAsStream("kweb_bootstrap.html")
                val bootstrapHtmlTemplate = IOUtils.toString(resourceStream, Charsets.UTF_8)
                        .replace("<!-- START HEADER PLACEHOLDER -->", startHeadBuilder.toString())
                        .replace("<!-- END HEADER PLACEHOLDER -->", endHeadBuilder.toString())

                // Setup default KWeb routing.
                get("/favicon.ico") {
                    call.response.status(HttpStatusCode.NotFound)
                    call.respondText("favicons not currently supported by kweb")
                }

                get("/{visitedUrl...}") {
                    val kwebSessionId = call.sessions.get<KwebSessionId>()
                    if (kwebSessionId == null) {
                        close@ (CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                        return@get
                    }

                    val remoteClientState = clients.getOrPut(kwebSessionId) {
                        RemoteClientState(id = kwebSessionId, clientConnection = KwebClientConnection.Caching())
                    }

                    val httpRequestInfo = HttpRequestInfo(call.request)

                    try {
                        if (debug) {
                            warnIfBlocking(maxTimeMs = maxPageBuildTimeMS, onBlock = { thread ->
                                val logStatementBuilder = StringBuilder()
                                logStatementBuilder.appendln("buildPage lambda must return immediately but has taken > $maxPageBuildTimeMS ms, appears to be blocking here:")
                                thread.stackTrace.pruneAndDumpStackTo(logStatementBuilder)
                                val logStatement = logStatementBuilder.toString()
                                logger.warn { logStatement }
                            }) {
                                buildPage(WebBrowser(kwebSessionId, httpRequestInfo, this@Kweb))
                                logger.debug { "Outbound message queue size after buildPage is ${(remoteClientState.clientConnection as Caching).queueSize()}" }
                            }
                        } else {
                            buildPage(WebBrowser(kwebSessionId, httpRequestInfo, this@Kweb))
                            logger.debug { "Outbound message queue size after buildPage is ${(remoteClientState.clientConnection as Caching).queueSize()}" }
                        }
                        for (plugin in plugins) {
                            execute(kwebSessionId, plugin.executeAfterPageCreation())
                        }

                        val initialCachedMessages = remoteClientState.clientConnection as Caching

                        remoteClientState.clientConnection = Caching()

                        val bootstrapHtml = bootstrapHtmlTemplate
                                .replace("--CLIENT-ID-PLACEHOLDER--", kwebSessionId.id)
                                .replace("<!-- BUILD PAGE PAYLOAD PLACEHOLDER -->", initialCachedMessages.read().map { "handleInboundMessage($it);" }.joinToString(separator = "\n"))

                        call.response.contentType(ContentType.Text.Html)
                        call.respond(bootstrapHtml)
                    } catch (nfe: NotFoundException) {
                        call.response.status(HttpStatusCode.NotFound)
                        call.respondText("URL ${call.request.uri} not found.", ContentType.parse("text/plain"))
                    } catch (e: Exception) {
                        call.response.status(HttpStatusCode.InternalServerError)
                        val logToken = random.nextLong().toString(16)
                        call.respondText("""
                        Internal Server Error.

                        Please include code $logToken in any message report to help us track it down.
""".trimIndent())
                        logger.error(e) { "Exception thrown while rendering page, code $logToken" }
                    }
                }

                webSocket("/ws") {
                    val kwebSessionId = call.sessions.get<KwebSessionId>()
                    if (kwebSessionId != null) {
                        logger.debug("Received websocket message for $kwebSessionId")

                        val session = clients.get(kwebSessionId) ?: throw RuntimeException("Unable to find session for $kwebSessionId")

                        val clientConnection = session.clientConnection
                        if (clientConnection is Caching) {
                            val webSocketClientConnection = KwebClientConnection.WebSocket(this)
                            session.clientConnection = webSocketClientConnection
                            clientConnection.read().forEach { webSocketClientConnection.send(it) }
                        }

                        try {
                            incoming.consumeEach { frame ->
                                if (frame is Frame.Text) {
                                    val message = gson.fromJson<Client2ServerMessage>(frame.readText())
                                    handleInboundMessage(session, message)
                                } else {
                                    logger.warn { "Unknown frame type: $frame" }
                                }
                            }
                        } finally {
                            logger.info("Ending WS session for client id $kwebSessionId")
                            clients.remove(kwebSessionId)
                        }
                    } else {
                        logger.warn {"No kweb sessionId found for websocket request, ignoring"}
                    }
                }
            }
        }

        server.start()
        logger.info {"KWeb is listening on port $port"}
    }

    private fun handleInboundMessage(fromClient: RemoteClientState, message: Client2ServerMessage) {
        logger.debug { "Message received to client id ${fromClient.id.id}" }
        if (message.error != null) {
            handleError(message.error, fromClient)
        } else if (message.hello != null) {
            logger.info { "Disregarding 'hello' message" }
        } else {
            when {
                message.callback != null -> {
                    val (resultId, result) = message.callback
                    val resultHandler = fromClient.handlers[resultId] ?: throw RuntimeException("No data handler for $resultId for client ${fromClient.id}")
                    resultHandler(result ?: "")
                }
            }
        }
    }

    private fun handleError(error: Client2ServerMessage.ErrorMessage, remoteClientState: RemoteClientState) {
        val debugInfo = remoteClientState.debugTokens[error.debugToken] ?: throw RuntimeException("DebugInfo message not found")
        val logStatementBuilder = StringBuilder()
        logStatementBuilder.appendln("JavaScript message: '${error.error.message}'")
        logStatementBuilder.appendln("Caused by ${debugInfo.action}: '${debugInfo.js}':")
        // TODO: Filtering the stacktrace like this seems a bit kludgy, although I can't think
        // TODO: of a specific reason why it would be bad.
        debugInfo.throwable.stackTrace.pruneAndDumpStackTo(logStatementBuilder)
        if (onError(debugInfo.throwable.stackTrace.toList(), error.error.message)) {
            logger.error(logStatementBuilder.toString())
        }
    }

    private fun applyPlugin(plugin: KWebPlugin,
                            appliedPlugins: MutableSet<KWebPlugin>,
                            endHeadBuilder: java.lang.StringBuilder,
                            startHeadBuilder: java.lang.StringBuilder,
                            routeHandler : Routing) {
        for (dependantPlugin in plugin.dependsOn) {
            if (!appliedPlugins.contains(dependantPlugin)) {
                applyPlugin(dependantPlugin, appliedPlugins, endHeadBuilder, startHeadBuilder, routeHandler)
                appliedPlugins.add(dependantPlugin)
            }
        }
        if (!appliedPlugins.contains(plugin)) {
            plugin.decorate(startHeadBuilder, endHeadBuilder)
            plugin.appServerConfigurator(routeHandler)
            appliedPlugins.add(plugin)
        }
    }

    private fun refreshAllPages() = async(CommonPool) {
        for (client in clients.values) {
            val message = S2CWebsocketMessage(
                    yourId = client.id.id,
                    execute = S2CWebsocketMessage.Execute("window.location.reload(true);"), debugToken = null)
            client.clientConnection.send(message.toJson())
        }
    }

    fun execute(clientId: KwebSessionId, javascript: String) {
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(javascript, "executing", Throwable()))
            dt
        }
        wsClientData.send(S2CWebsocketMessage(yourId = clientId.id, debugToken = debugToken, execute = S2CWebsocketMessage.Execute(javascript)))
    }

    fun executeWithCallback(clientId: KwebSessionId, javascript: String, callbackId: Int, handler: (String) -> Unit) {
        // TODO: Should return handle which can be used for cleanup of event listeners
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(javascript, "executing with callback", Throwable()))
            dt
        }
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(S2CWebsocketMessage(yourId = clientId.id, execute = S2CWebsocketMessage.Execute(javascript), debugToken = debugToken))
    }

    fun removeCallback(clientId: KwebSessionId, callbackId: Int) {
        clients.get(clientId)?.handlers?.remove(callbackId)
    }

    fun evaluate(clientId: KwebSessionId, expression: String, handler: (String) -> Unit) {
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Failed to evaluate JavaScript because client id $clientId not found")
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(expression, "evaluating", Throwable()))
            dt
        }
        val callbackId = Math.abs(random.nextInt())
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(S2CWebsocketMessage(yourId = clientId.id, evaluate = S2CWebsocketMessage.Evaluate(expression, callbackId), debugToken = debugToken))
    }

    override fun close() {
        server.stop(0, 0, TimeUnit.SECONDS)
    }

}

private data class RemoteClientState(val id: KwebSessionId, @Volatile var clientConnection: KwebClientConnection, val handlers: MutableMap<Int, (String) -> Unit> = HashMap(), val debugTokens: MutableMap<String, DebugInfo> = HashMap()) {
    fun send(message: S2CWebsocketMessage) {
        clientConnection.send(gson.toJson(message))
    }

    override fun toString() = "RemoteClientState(id = ${id.id})"
}

/**
 * @param request This is the raw ApplicationRequest object to [Ktor](https://github.com/Kotlin/ktor), the HTTP
 *                library used by Kweb.  It can be used to read various information about the inbound HTTP request,
 *                however you should use properties of [HttpRequestInfo] directly instead if possible.
 */
data class HttpRequestInfo(val request: ApplicationRequest) {
    val requestedUrl : URL by lazy {
        val urlAsString = with(request.origin) {
            "$scheme://$host:$port$uri"
        }
        URL.parse(urlAsString)
    }

    val cookies = request.cookies

    val remoteHost = request.origin.remoteHost

    val userAgent = request.headers["User-Agent"]
}

data class DebugInfo(val js: String, val action : String, val throwable: Throwable)

data class S2CWebsocketMessage(
        val yourId: String, // TODO: Remove this, client shouldn't need it as it is in cookie
        val debugToken: String?,
        val execute: Execute? = null,
        val evaluate: Evaluate? = null
) {
    data class Execute(val js: String)
    data class Evaluate(val js: String, val callbackId: Int)
}

data class Client2ServerMessage(
        val id: String,
        val hello: Boolean? = true,
        val error: ErrorMessage? = null,
        val callback: C2SCallback?
) {
    data class ErrorMessage(val debugToken: String, val error: Error) {
        data class Error(val name: String, val message: String)
    }

    data class C2SCallback(val callbackId: Int, val data: String?)
}

data class KwebSessionId(val id: String)
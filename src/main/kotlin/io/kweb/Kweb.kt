package io.kweb

import io.kweb.browserConnection.OutboundChannel
import io.kweb.dev.hotswap.KwebHotswapPlugin
import io.kweb.plugins.KWebPlugin
import io.mola.galimatias.URL
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.apache.commons.io.IOUtils
import org.jetbrains.ktor.application.ApplicationRequest
import org.jetbrains.ktor.application.call
import org.jetbrains.ktor.application.install
import org.jetbrains.ktor.features.DefaultHeaders
import org.jetbrains.ktor.features.origin
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.request.uri
import org.jetbrains.ktor.response.contentType
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.routing
import org.jetbrains.ktor.websocket.Frame
import org.jetbrains.ktor.websocket.WebSocket
import org.jetbrains.ktor.websocket.readText
import org.jetbrains.ktor.websocket.webSocket
import java.io.Closeable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by ian on 12/31/16.
 */

typealias OneTime = Boolean
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
           val appServerConfigurator: (org.jetbrains.ktor.routing.Routing) -> Unit = {},
           val onError : ((List<StackTraceElement>, io.kweb.JavaScriptError) -> io.kweb.LogError) = { _, _ ->  true},
           val maxPageBuildTimeMS : Long = 200,
           val buildPage: WebBrowser.() -> Unit
) : Closeable {
    companion object: mu.KLogging()

    private val server: org.jetbrains.ktor.netty.NettyApplicationHost
    private val clients: MutableMap<String, io.kweb.WSClientData>
    private val mutableAppliedPlugins: MutableSet<io.kweb.plugins.KWebPlugin> = java.util.HashSet()
    val appliedPlugins: Set<io.kweb.plugins.KWebPlugin> get() = mutableAppliedPlugins

    init {

        //TODO: Need to do housekeeping to deleteIfExists old client data
        clients = java.util.concurrent.ConcurrentHashMap<String, WSClientData>()

        val startHeadBuilder = StringBuilder()
        val endHeadBuilder = StringBuilder()

        if (refreshPageOnHotswap) {
            KwebHotswapPlugin.addHotswapReloadListener({refreshAllPages()})
        }

        server= org.jetbrains.ktor.netty.embeddedNettyServer(port) {
            install(DefaultHeaders)
            routing {

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
                    val newClientId = Math.abs(random.nextLong()).toString(16)
                    val outboundBuffer = OutboundChannel.TemporarilyStoringChannel()
                    val wsClientData = WSClientData(id = newClientId, outboundChannel = outboundBuffer)
                    clients.put(newClientId, wsClientData)
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
                                buildPage(WebBrowser(newClientId, httpRequestInfo, this@Kweb))
                                logger.debug { "Outbound message queue size after buildPage is ${outboundBuffer.queueSize()}" }
                            }
                        } else {
                            buildPage(WebBrowser(newClientId, httpRequestInfo, this@Kweb))
                            logger.debug { "Outbound message queue size after buildPage is ${outboundBuffer.queueSize()}" }
                        }
                        for (plugin in plugins) {
                            execute(newClientId, plugin.executeAfterPageCreation())
                        }
                        wsClientData.outboundChannel = OutboundChannel.TemporarilyStoringChannel()

                        val bootstrapHtml = bootstrapHtmlTemplate
                                .replace("--CLIENT-ID-PLACEHOLDER--", newClientId)
                                .replace("<!-- BUILD PAGE PAYLOAD PLACEHOLDER -->", outboundBuffer.read().map { "handleInboundMessage($it);" }.joinToString(separator = "\n"))

                        // TODO replace with ktor type.
                        call.response.contentType("text/html")
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
                    handle { frame ->
                        if (frame is Frame.Text) {
                            val message = gson.fromJson(frame.readText(), C2SWebsocketMessage::class.java)
                            handleInboundMessage(this, message)
                        }
                    }
                }
            }
        }

        server!!.start()
        logger.info {"KWeb is listening on port $port"}
    }

    private fun handleInboundMessage(socket: WebSocket, message: C2SWebsocketMessage) {
        val wsClientData = clients[message.id] ?: throw RuntimeException("Message with id ${message.id} received, but id is unknown")
        logger.debug { "Message received to client id ${wsClientData.id}" }
        if (message.error != null) {
            handleError(message.error, wsClientData)
        } else if (message.hello != null) {
            handleHello(socket, wsClientData)
        } else {
            val clientId = message.id
            val clientData = clients[clientId] ?: throw RuntimeException("No handler found for client $clientId")
            when {
                message.callback != null -> {
                    val (resultId, result) = message.callback
                    val resultHandler = clientData.handlers[resultId] ?: throw RuntimeException("No data handler for $resultId for client $clientId")
                    resultHandler(result ?: "")
                }
            }
        }
    }

    private fun handleHello(socket: WebSocket, wsClientData: WSClientData) = async(CommonPool) {
        val tempQueue = wsClientData.outboundChannel as OutboundChannel.TemporarilyStoringChannel
        wsClientData.outboundChannel = OutboundChannel.WSChannel(socket)
        tempQueue.read().forEach { wsClientData.outboundChannel.send(it) }
    }

    private fun handleError(error: C2SWebsocketMessage.ErrorMessage, wsClientData: WSClientData) {
        val debugInfo = wsClientData.debugTokens[error.debugToken] ?: throw RuntimeException("DebugInfo message not found")
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
                    yourId = client.id,
                    execute = S2CWebsocketMessage.Execute("window.location.reload(true);"), debugToken = null)
            client.outboundChannel.send(message.toJson())
        }
    }

    fun execute(clientId: String, javascript: String) {
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(javascript, "executing", Throwable()))
            dt
        }
        wsClientData.send(S2CWebsocketMessage(yourId = clientId, debugToken = debugToken, execute = S2CWebsocketMessage.Execute(javascript)))
    }

    fun executeWithCallback(clientId: String, javascript: String, callbackId: Int, handler: (String) -> Unit) {
        // TODO: Should return handlem which can be used for cleanup of event listeners
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(javascript, "executing with callback", Throwable()))
            dt
        }
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(S2CWebsocketMessage(yourId = clientId, execute = S2CWebsocketMessage.Execute(javascript), debugToken = debugToken))
    }

    fun evaluate(clientId: String, expression: String, handler: (String) -> Unit) {
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        val debugToken: String? = if (!debug) null else {
            val dt = Math.abs(random.nextLong()).toString(16)
            wsClientData.debugTokens.put(dt, DebugInfo(expression, "evaluating", Throwable()))
            dt
        }
        val callbackId = Math.abs(random.nextInt())
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(S2CWebsocketMessage(clientId, evaluate = S2CWebsocketMessage.Evaluate(expression, callbackId), debugToken = debugToken))
    }

    override fun close() {
        server.stop(0, 0, TimeUnit.SECONDS)
    }

}

private data class WSClientData(val id: String, @Volatile var outboundChannel: OutboundChannel, val handlers: MutableMap<Int, (String) -> Unit> = HashMap(), val debugTokens: MutableMap<String, DebugInfo> = HashMap()) {
    fun send(message: S2CWebsocketMessage) {
        outboundChannel.send(gson.toJson(message))
    }
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
        val yourId: String,
        val debugToken: String?,
        val execute: Execute? = null,
        val evaluate: Evaluate? = null
) {
    data class Execute(val js: String)
    data class Evaluate(val js: String, val callbackId: Int)
}

data class C2SWebsocketMessage(
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

package com.github.sanity.kweb

import com.github.sanity.kweb.browserConnection.OutboundChannel
import com.github.sanity.kweb.plugins.KWebPlugin
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.apache.commons.io.IOUtils
import org.wasabifx.wasabi.app.AppConfiguration
import org.wasabifx.wasabi.app.AppServer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by ian on 12/31/16.
 */

typealias OneTime = Boolean

/*
TODO: Hi Ian, pushed a change to wasabi, if your interested you should be able to create a
subclass of wasabi's AppServer and override init and set routeLocator to something that
implements wasabi's RouteLocator interface, only caveat atm is autooptions/cors support
wont be happy. But I thought for your use case it would enable you to move things along
at your end till its properly configurable
 */

class KWeb(val port: Int,
           val plugins: List<KWebPlugin> = Collections.emptyList(),
           val appServerConfigurator: (AppServer) -> Unit = {},
           val buildPage: RootReceiver.() -> Unit
) {
    private val server = AppServer(AppConfiguration(port = port))
    private val clients: MutableMap<String, WSClientData>
    private val mutableAppliedPlugins: MutableSet<KWebPlugin> = HashSet()
    val appliedPlugins: Set<KWebPlugin> get() = mutableAppliedPlugins

    init {
        appServerConfigurator.invoke(server)

        //TODO: Need to do housekeeping to delete old client data
        clients = ConcurrentHashMap<String, WSClientData>()

        val startHeadBuilder = StringBuilder()
        val endHeadBuilder = StringBuilder()

        for (plugin in plugins) {
            applyPlugin(plugin = plugin, appliedPlugins = mutableAppliedPlugins, endHeadBuilder = endHeadBuilder, startHeadBuilder = startHeadBuilder, appServer = server)
        }

        val bootstrapHtmlTemplate = IOUtils.toString(javaClass.getResourceAsStream("kweb_bootstrap.html"), Charsets.UTF_8)
                .replace("<!-- START HEADER PLACEHOLDER -->", startHeadBuilder.toString())
                .replace("<!-- END HEADER PLACEHOLDER -->", endHeadBuilder.toString())

        server.get("/", {
            val newClientId = Math.abs(random.nextLong()).toString(16)
            val outboundBuffer = OutboundChannel.TemporarilyStoringChannel()
            val wsClientData = WSClientData(id = newClientId, outboundChannel = outboundBuffer)
            clients.put(newClientId, wsClientData)
            buildPage(RootReceiver(newClientId, this@KWeb))
            for (plugin in plugins) {
                execute(newClientId, plugin.executeAfterPageCreation())
            }
            wsClientData.outboundChannel = OutboundChannel.TemporarilyStoringChannel()

            val bootstrapHtml = bootstrapHtmlTemplate
                    .replace("--CLIENT-ID-PLACEHOLDER--", newClientId)
                    .replace("<!-- BUILD PAGE PAYLOAD PLACEHOLDER -->", outboundBuffer.read().map {"handleInboundMessage($it);"} . joinToString(separator = "\n"))
            response.send(bootstrapHtml)
        })

        server.channel("/ws") {
            if (frame is TextWebSocketFrame) {
                val message = gson.fromJson((frame as TextWebSocketFrame).text(), C2SWebsocketMessage::class.java)
                handleInboundMessage(ctx!!, message)
            }
        }
        server.start(wait = false)
    }

    private fun handleInboundMessage(ctx: ChannelHandlerContext, message: C2SWebsocketMessage) {
        if (message.hello != null) {
            val wsClientData = clients[message.id] ?: throw RuntimeException("Message with id ${message.id} received, but id is unknown")
            val tempQueue = wsClientData.outboundChannel as OutboundChannel.TemporarilyStoringChannel
            wsClientData.outboundChannel = OutboundChannel.WSChannel(ctx.channel())
            tempQueue.read().forEach { wsClientData.outboundChannel.send(it) }
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

    private fun applyPlugin(plugin: KWebPlugin,
                            appliedPlugins: MutableSet<KWebPlugin>,
                            endHeadBuilder: java.lang.StringBuilder,
                            startHeadBuilder: java.lang.StringBuilder,
                            appServer : AppServer) {
        for (dependantPlugin in plugin.dependsOn) {
            if (!appliedPlugins.contains(dependantPlugin)) {
                applyPlugin(dependantPlugin, appliedPlugins, endHeadBuilder, startHeadBuilder, appServer)
                appliedPlugins.add(dependantPlugin)
            }
        }
        if (!appliedPlugins.contains(plugin)) {
            plugin.decorate(startHeadBuilder, endHeadBuilder)
            plugin.appServerConfigurator(appServer)
            appliedPlugins.add(plugin)
        }
    }


    fun execute(clientId: String, message: String) {
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        wsClientData.send(S2CWebsocketMessage(yourId = clientId, execute = Execute(message)))
    }

    fun executeWithCallback(clientId: String, js: String, callbackId: Int, handler: (String) -> Unit) {
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(S2CWebsocketMessage(yourId = clientId, execute = Execute(js)))
    }

    fun evaluate(clientId: String, expression: String, handler: (String) -> Unit) {
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        val callbackId = Math.abs(random.nextInt())
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(S2CWebsocketMessage(clientId, evaluate = Evaluate(expression, callbackId)))
    }

}

private data class WSClientData(val id: String, @Volatile var outboundChannel: OutboundChannel, val handlers: MutableMap<Int, (String) -> Unit> = HashMap()) {
    fun send(message: S2CWebsocketMessage) {
        outboundChannel.send(gson.toJson(message))
    }
}

data class S2CWebsocketMessage(
        val yourId: String,
        val execute: Execute? = null,
        val evaluate: Evaluate? = null
)

data class Execute(val js: String)

data class Evaluate(val js: String, val callbackId: Int)

data class C2SWebsocketMessage(
        val id: String,
        val hello: Boolean? = true,
        val callback: C2SCallback?
)

data class C2SCallback(val callbackId: Int, val data: String?)


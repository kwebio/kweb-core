package com.github.sanity.kweb

import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.wasabifx.wasabi.app.AppConfiguration
import org.wasabifx.wasabi.app.AppServer
import org.wasabifx.wasabi.protocol.websocket.respond
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by ian on 12/31/16.
 */

typealias OneTime = Boolean

class KWeb(val port: Int, val startHead: String = "", val endHead: String = "", override val rh: CoreReceiver.() -> Unit) : ClientConduit(rh) {
    private val server = AppServer(AppConfiguration(port = port))
    private val clients: MutableMap<String, WSClientData>

    init {
        //TODO: Need to do housekeeping to delete old client data
        clients = ConcurrentHashMap<String, WSClientData>()

        val bootstrapHtml = String(Files.readAllBytes(Paths.get(javaClass.getResource("kweb_bootstrap.html").toURI())), StandardCharsets.UTF_8)
                .replace("<!-- START HEADER PLACEHOLDER -->", startHead)
                .replace("<!-- END HEADER PLACEHOLDER -->", endHead)

        server.get("/", {
            response.send(bootstrapHtml)
        })

        server.channel("/ws") {
            if (frame is TextWebSocketFrame) {
                val message = gson.fromJson((frame as TextWebSocketFrame).text(), C2SWebsocketMessage::class.java)
                if (message.hello != null) {
                    val newClientId = Math.abs(random.nextLong()).toString(16)
                    val wsClientData = WSClientData(id = newClientId, clientChannel = ctx!!.channel())
                    clients.put(newClientId, wsClientData)
                    wsClientData.send(S2CWebsocketMessage(newClientId))
                    rh.invoke(CoreReceiver(newClientId, this@KWeb))
                } else {
                    val clientId = message.id ?: throw RuntimeException("Message has no id but is not hello")
                    val clientData = clients[clientId] ?: throw RuntimeException("No handler found for client $clientId")
                    when {
                        message.callback != null -> {
                            val (resultId, result) = message.callback
                            val resultHandler = clientData.handlers[resultId] ?: throw RuntimeException("No data handler for $resultId for client $clientId")
                            resultHandler(result)
                        }
                    }
                }
            }
        }
        server.start()
    }


    override fun execute(clientId: String, message: String) {
        //println("execute($js)")
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        wsClientData.send(S2CWebsocketMessage(yourId = clientId, execute = Execute(message)))
    }

    override fun executeWithCallback(clientId: String, js: String, callbackId: Int, handler: (String) -> Unit) {
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(S2CWebsocketMessage(yourId = clientId, execute = Execute(js)))
    }

    override fun evaluate(clientId: String, expression: String, handler: (String) -> Unit) {
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        val callbackId = Math.abs(random.nextInt())
        wsClientData.handlers.put(callbackId, handler)
        wsClientData.send(S2CWebsocketMessage(clientId, evaluate = Evaluate(expression, callbackId)))
    }

}


private data class WSClientData(val id: String, var clientChannel: Channel, val handlers: MutableMap<Int, (String) -> Unit> = HashMap()) {
    fun send(message: S2CWebsocketMessage) {
        respond(clientChannel, TextWebSocketFrame(gson.toJson(message)))
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
        val id: String?,
        val hello: Boolean? = true,
        val callback: C2SCallback?
)

data class C2SCallback(val callbackId: Int, val data: String)

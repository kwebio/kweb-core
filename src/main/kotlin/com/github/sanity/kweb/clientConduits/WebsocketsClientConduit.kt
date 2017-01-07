package com.github.sanity.kweb.clientConduits

import com.github.sanity.kweb.random
import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.wasabifx.wasabi.app.AppConfiguration
import org.wasabifx.wasabi.app.AppServer
import org.wasabifx.wasabi.protocol.websocket.respond
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * Created by ian on 12/31/16.
 */

typealias OneTime = Boolean

class WebsocketsClientConduit(val port: Int, override open val rh: CCReceiver.() -> Boolean) : ClientConduit(rh) {
    private val server = AppServer(AppConfiguration(port = port))
    private val clients : MutableMap<String, WSClientData>

    init {

        clients = HashMap<String, WSClientData>()

        val bootstrapHtml = String(Files.readAllBytes(Paths.get(javaClass.getResource("bootstrap.html").toURI())), StandardCharsets.UTF_8)

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
                    rh.invoke(CCReceiver(newClientId, this@WebsocketsClientConduit))
                } else {
                    val clientId = message.id ?: throw RuntimeException("Message has no id but is not hello")
                    val clientData = clients[clientId] ?: throw RuntimeException("No handler found for client $clientId")
                    when {
                        message.sendResult != null -> {
                            val (resultId, result) = message.sendResult
                            val resultHandler = clientData.handlers[resultId] ?: throw RuntimeException("No result handler for $resultId for client $clientId")
                            val oneTime = resultHandler(result)
                            if (oneTime) {
                                clientData.handlers.remove(resultId)
                            }
                        }
                    }
                }
            }
        }
        server.start()
    }


    override fun execute(clientId: String, js: String) {
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        wsClientData.send(S2CWebsocketMessage(yourId = clientId, execute = Execute(js)))
    }

    override fun evaluate(clientId: String, expression: String, handler: (String) -> Boolean) {
        val wsClientData = clients.get(clientId) ?: throw RuntimeException("Client id $clientId not found")
        val handlerId = Math.abs(random.nextInt())
        wsClientData.handlers.put(handlerId, handler)
        wsClientData.send(S2CWebsocketMessage(clientId, evaluate = Evaluate(expression, handlerId)))
    }

}


private data class WSClientData(val id: String, var clientChannel: Channel, val handlers: MutableMap<Int, (String) -> OneTime> = HashMap()) {
    fun send(message : S2CWebsocketMessage) {
        respond(clientChannel, TextWebSocketFrame(gson.toJson(message)))
    }
}

data class S2CWebsocketMessage(
        val yourId: String,
        val execute: Execute? = null,
        val evaluate: Evaluate? = null
)

data class Execute(val js: String)

data class Evaluate(val js: String, val responseId: Int)

data class C2SWebsocketMessage(
        val id: String?,
        val hello: Boolean? = true,
        val sendResult: C2SSendResult?
)

data class C2SSendResult(val responseId: Int, val result: String)


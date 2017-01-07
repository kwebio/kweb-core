package com.github.sanity.kweb.clientConduits

import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.wasabifx.wasabi.app.AppConfiguration
import org.wasabifx.wasabi.app.AppServer
import org.wasabifx.wasabi.protocol.websocket.respond
import java.util.*

/**
 * Created by ian on 12/31/16.
 */
class WebsocketsClientConduit(val port: Int, override open val rh: CCReceiver.() -> Boolean) : ClientConduit(rh) {
    private val server = AppServer(AppConfiguration(port = port))


    init {
        server.get("/", {
            response.send(
                    //language=HTML
                    """
<html>
<head>
    <script language="JavaScript">

        function kweb_respond(responseId, response) {

        }
    </script>
</head>
<body onload="longPoll()">

</body>
</html>
                    """
            )
        })

        server.channel("/ws") {
            if (frame is TextWebSocketFrame) {
                val message = gson.fromJson((frame as TextWebSocketFrame).text(), C2SWebsocketMessage::class.java)
                if (message.hello != null) {
                    val newClientId = Math.abs(random.nextLong()).toString(16)
                    clients.put(newClientId, WSClientData(clientChannel = ctx!!.channel()))
                    respond(ctx!!.channel(), TextWebSocketFrame(gson.toJson(S2CWebsocketMessage(yourId = newClientId))))
                    rh.invoke(CCReceiver(newClientId, this@WebsocketsClientConduit))
                } else {
                    val clientId = message.id ?: throw RuntimeException("Message has no id but is not hello")
                    val clientData = clients[clientId] ?: throw RuntimeException("No handler found for client $clientId")
                    when {
                        message.sendResult != null -> {
                            val (resultId, result) = message.sendResult
                            val resultHandler = clientData.handlers[resultId] ?: throw RuntimeException("No result handler for $resultId for client $clientId")
                            val shouldDelete = resultHandler(result)
                            if (shouldDelete) {
                                clientData.handlers.remove(resultId)
                            }
                        }
                    }
                }
            }
        }

    }

    override fun send(clientId: String, messages: List<ClientMessage>) {

    }

    private val random = Random()

    private val clients = HashMap<String, WSClientData>()


}

private data class WSClientData(var clientChannel: Channel, val handlers: MutableMap<Int, (String) -> Boolean> = HashMap())

data class S2CWebsocketMessage(
        val yourId: String?
)

data class C2SWebsocketMessage(
        val id: String?,
        val hello: Boolean? = true,
        val sendResult: C2SSendResult?
)

data class C2SSendResult(val resultId: Int, val result: String)


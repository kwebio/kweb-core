package com.github.sanity.kweb.clientConduits

/**
 * Created by ian on 12/31/16.

class WebsocketsClientConduit(val port: Int, override open val rh: CCReceiver.() -> Boolean) : ClientConduit(rh) {
    private val server = AppServer(AppConfiguration(port = port))

    init {
        server.get("/", {
            val newClientId = Math.abs(random.nextLong())
            response.sendWithResponseHandler(
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
            rh.invoke(CCReceiver(newClientId, this@InefficientClientConduit))
        })
        server.channel("/ws") {

        }
    }

    override fun sendWithResponseHandler(clientId: Long, messages: List<ClientMessage>) {

    }

    override fun sendWithResponseHandler(clientId: Long, message: ClientMessage) {
        sendWithResponseHandler(clientId, Collections.singletonList(message))
    }

    private val random = Random()

    private val clients = HashMap<Long, WSClientData>()


}

private data class WSClientData(val toSend: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue(), var thread: Thread? = null, val handlers: MutableMap<Long, (String) -> Boolean> = HashMap())

        */
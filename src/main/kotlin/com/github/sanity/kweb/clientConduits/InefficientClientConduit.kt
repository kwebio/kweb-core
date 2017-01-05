package com.github.sanity.kweb.clientConduits

import org.wasabifx.wasabi.app.AppConfiguration
import org.wasabifx.wasabi.app.AppServer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread

/**
 * Created by ian on 12/31/16.
 */
class InefficientClientConduit(val port: Int, override open val rh: CCReceiver.() -> Boolean) : ClientConduit(rh) {
    override fun send(clientId: Long, messages: List<ClientMessage>) {
        val clientData = clients.computeIfAbsent(clientId, { ClientData() })!!
        for (message in messages) {
            if (message.responseHandler != null) {
                if (!message.msg.contains("%respond%(")) {
                    throw IllegalArgumentException("No %respond%( found in ${message.msg}")
                }

                val responseId = Math.abs(random.nextLong())
                val enrichedMessage = message.msg.replace("%respond%(", "kweb_respond(\"$responseId\",")
                clientData.handlers.put(responseId, message.responseHandler)
                clientData.toSend.add(enrichedMessage)
            } else {
                clientData.toSend.add(message.msg)
            }
        }
        clientData.thread?.interrupt()
    }

    override fun send(clientId: Long, message: ClientMessage) {
        send(clientId, Collections.singletonList(message))
    }

    private val random = Random()

    private val server = AppServer(AppConfiguration(port = port))

    private val clients = HashMap<Long, ClientData>()

    init {
        server.get("/", {
            val newClientId = Math.abs(random.nextLong())
            response.send(
                    """
<html>
<head>
    <script language="JavaScript">
        var requestId = 0
        function longPoll() {
            var xhr = new XMLHttpRequest();
            var path="/msg/$newClientId/"+requestId
            xhr.open('GET', path, true);
            requestId++;
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4 && xhr.status == 200) {
                    console.log("eval("+xhr.responseText+")")
                    eval(xhr.responseText)
                    longPoll();
                }
            }
            xhr.send()
        }
        function kweb_respond(responseId, response) {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', '/response/$newClientId/'+responseId, true)
            xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded")
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4 && xhr.status == 200) {
                }
            }
            xhr.send("response="+escape(response))
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

        server.get("/msg/:clientid/:requestid", {
            val clientId = request.routeParams["clientid"]!!.toLong()
            val clientData = clients.computeIfAbsent(clientId, { ClientData() })
            val toSend = clientData.toSend
            while (toSend.isEmpty()) {
                try {
                    if (clientData.thread != null) {
                        clientData.thread?.interrupt()
                    }
                    clientData.thread = Thread.currentThread()
                    Thread.sleep(60000) // being called again for client during sleep, thread is overwritten
                } catch (e: InterruptedException) {

                }
            }
            val textToSend = StringBuffer()
            while (toSend.isNotEmpty()) {
                textToSend.append(toSend.poll())
            }
            response.send(textToSend.toString(), "text/plain")
        })

        server.post("/response/:clientid/:responseid", {
            val clientId = request.routeParams["clientid"]!!.toLong()
            val responseId = request.routeParams["responseid"]!!.toLong()
            val handlers = clients.get(clientId)!!.handlers
            val handler = handlers.get(responseId)
            if (handler != null) {
                val shouldDelete = handler(request.bodyParams["response"].toString())
                if (shouldDelete) {
                    handlers.remove(responseId)
                }
            }
            response.send("success", "text/plain")
        })

        thread {
            server.start()
        }
    }


}

private data class ClientData(val toSend: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue(), var thread: Thread? = null, val handlers: ConcurrentHashMap<Long, (String) -> Boolean> = ConcurrentHashMap())
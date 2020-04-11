package kweb.client

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.Frame.Text
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

sealed class ClientConnection {
    abstract fun send(message: String)

    //@ObsoleteCoroutinesApi // TODO: For Channel.consumeEach, which will apparently become obsolete
    class WebSocket(private val channel: WebSocketSession) : ClientConnection() {

        @Volatile var sendCount = 0

        private val sendBuffer = Channel<Frame>(capacity = 1000)

        init {
             GlobalScope.launch {
                 for (frame in sendBuffer) {
                     channel.send(frame)
                 }
            }

        }

        override fun send(message: String) {
            runBlocking {
                sendBuffer.send(Text(message))
            }
            sendCount++
        }
    }

    class Caching : ClientConnection() {
        private @Volatile
        var queue: ConcurrentLinkedQueue<String>? = ConcurrentLinkedQueue()

        override fun send(message: String) {
            logger.debug("Caching '$message' as websocket isn't yet available")
            queue.let {
                it?.add(message) ?: error("Can't write to queue after it has been read")
            }
        }

        val size = queue?.size ?: 0

        fun read(): List<String> {
            queue.let {
                if (it == null) error("Queue can only be read once")
                else {
                    val r = it.toList()
                    queue = null
                    return r
                }
            }
        }

        fun queueSize() = queue?.size
    }

}
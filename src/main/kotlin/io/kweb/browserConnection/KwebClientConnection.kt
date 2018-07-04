package io.kweb.browserConnection

import io.ktor.http.cio.websocket.Frame.Text
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

sealed class KwebClientConnection {
    abstract fun send(message: String)

    class WebSocket(private val channel: WebSocketSession) : KwebClientConnection() {

        override fun send(message: String) {
            launch {
                channel.send(Text(message))
            }
        }

    }

    class Caching : KwebClientConnection() {
        private @Volatile
        var queue: ConcurrentLinkedQueue<String>? = ConcurrentLinkedQueue()

        override fun send(message: String) {
            logger.debug("Caching '$message' as websocket isn't yet available")
            queue.let {
                it?.add(message) ?: throw RuntimeException("Can't write to queue after it has been read")
            }
        }

        fun read(): List<String> {
            queue.let {
                if (it == null) throw RuntimeException("Queue can only be read once")
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
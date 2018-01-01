package io.kweb.browserConnection

import io.ktor.websocket.*
import kotlinx.coroutines.experimental.runBlocking
import java.util.concurrent.ConcurrentLinkedQueue

sealed class KwebClientConnection {
    abstract fun send(message: String)

    class WebSocket(private val channel: WebSocketSession) : KwebClientConnection() {
        override fun send(message: String) {
            runBlocking {
                channel.send(Frame.Text(message))
            }
        }

    }

    class Caching : KwebClientConnection() {
        private @Volatile var queue: ConcurrentLinkedQueue<String>? = ConcurrentLinkedQueue<String>()

        override fun send(message: String) {
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
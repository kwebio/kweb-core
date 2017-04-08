package io.kweb.browserConnection

import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.ktor.websocket.Frame
import org.jetbrains.ktor.websocket.WebSocket
import java.util.concurrent.ConcurrentLinkedQueue

sealed class OutboundChannel {
    abstract fun send(message: String)

    class WSChannel(private val channel: WebSocket) : OutboundChannel() {
        override fun send(message: String) {
            runBlocking {
                channel.send(Frame.Text(message))
            }
        }

    }

    class TemporarilyStoringChannel() : OutboundChannel() {
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
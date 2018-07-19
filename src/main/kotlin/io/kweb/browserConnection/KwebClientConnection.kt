package io.kweb.browserConnection

import io.ktor.http.cio.websocket.*
import io.ktor.http.cio.websocket.Frame.Text
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.*
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

sealed class KwebClientConnection {
    abstract fun send(message: String)

    class WebSocket(private val channel: WebSocketSession) : KwebClientConnection() {

        @Volatile var sendCount = 0

        private val sendBuffer = ArrayChannel<Frame>(capacity = 1000)

        init {
            launch {
                sendBuffer.consumeEach { channel.outgoing.send(it) }
            }

        }

        override fun send(message: String) {
            logger.debug("Start message send: $message on channel isFull: ${channel.outgoing.isFull}  isClosedForSend: ${channel.outgoing.isClosedForSend}")
            runBlocking {
                sendBuffer.send(Text(message))
            }
            sendCount++
            logger.debug("End message send: $message")
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
package kweb.client

import io.ktor.websocket.*
import io.ktor.websocket.Frame.Text
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kweb.state.CloseReason
import mu.two.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val logger = KotlinLogging.logger {}

sealed class ClientConnection {

    abstract fun send(message: String)

    //@ObsoleteCoroutinesApi // TODO: For Channel.consumeEach, which will apparently become obsolete
    class WebSocket(private val channel: WebSocketSession) : ClientConnection() {

        @Volatile
        var sendCount = 0

        private val sendBuffer = Channel<Frame>(capacity = 1000)

        init {
            channel.launch {
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

        fun close(reason : io.ktor.websocket.CloseReason) {
            runBlocking {
                channel.close(reason)
                sendBuffer.close()
            }
        }
    }

    class Caching : ClientConnection() {
        private val queue = ConcurrentLinkedQueue<String>()
        private val lock = ReentrantLock()
        private val isRead = AtomicBoolean(false)

        override fun send(message: String) {
            lock.withLock {
                if (isRead.get()) {
                    error("Can't write to queue after it has been read")
                } else {
                    logger.debug("Caching '$message' as websocket isn't yet available")
                    queue.add(message)
                }
            }
        }

        fun read(): List<String> {
            lock.withLock {
                if (isRead.get()) {
                    error("Queue can only be read once")
                } else {
                    isRead.set(true)
                    return ArrayList<String>(queue).also {
                        queue.clear()
                    }
                }
            }
        }

        fun queueSize(): Int {
            lock.withLock {
                return queue.size
            }
        }
    }


}
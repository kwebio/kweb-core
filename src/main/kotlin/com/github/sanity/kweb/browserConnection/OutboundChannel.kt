package com.github.sanity.kweb.browserConnection

import org.jetbrains.ktor.websocket.Frame
import org.jetbrains.ktor.websocket.WebSocket
import java.util.concurrent.ConcurrentLinkedQueue

sealed class OutboundChannel {
    abstract suspend fun send(message: String)

    class WSChannel(private val channel: WebSocket) : OutboundChannel() {
        override suspend fun send(message: String) {
            channel.send(Frame.Text(message))
        }

    }

    class TemporarilyStoringChannel() : OutboundChannel() {
        private @Volatile var queue: ConcurrentLinkedQueue<String>? = ConcurrentLinkedQueue<String>()

        override suspend fun send(message: String) {
            (queue ?: throw RuntimeException("Can't write to queue after it has been read")).add(message)
        }

        fun read(): List<String> {
            val r = (queue ?: throw RuntimeException("Queue can only be read once")).toList()
            queue = null
            return r
        }

        fun queueSize() = queue?.size
    }
}
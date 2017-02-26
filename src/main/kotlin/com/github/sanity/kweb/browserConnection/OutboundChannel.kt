package com.github.sanity.kweb.browserConnection

import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.wasabifx.wasabi.protocol.websocket.respond
import java.util.concurrent.ConcurrentLinkedQueue

sealed class OutboundChannel {
    abstract fun send(message: String)

    class WSChannel(private val channel: Channel) : OutboundChannel() {
        override fun send(message: String) {
            respond(channel, TextWebSocketFrame(message))
        }

    }

    class TemporarilyStoringChannel() : OutboundChannel() {
        private @Volatile var queue: ConcurrentLinkedQueue<String>? = ConcurrentLinkedQueue<String>()

        override fun send(message: String) {
            (queue ?: throw RuntimeException("Can't write to queue after it has been read")).add(message)
        }

        fun read(): List<String> {
            val r = (queue ?: throw RuntimeException("Queue can only be read once")).toList()
            queue = null
            return r
        }
    }
}
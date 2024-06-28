package kweb.client

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kweb.DebugInfo
import kweb.util.random
import mu.two.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private var logger = KotlinLogging.logger {}

data class RemoteClientState(val id: String, val initialClientConnection: ClientConnection,
                             val eventHandlers: MutableMap<Int, (JsonElement) -> Unit> = HashMap(),
                             val onCloseHandlers : ConcurrentHashMap<Int, () -> Unit> = ConcurrentHashMap(),
                             val debugTokens: MutableMap<String, DebugInfo> = HashMap(), var lastModified: Instant = Instant.now(),
                             var onMessageFunction: ((data: JsonElement?) -> Unit)? = null) {

    private @Volatile var clientConnection = initialClientConnection

    fun getClientConnection() = clientConnection

    fun updateClientConnection(newClientConnection: ClientConnection) {
        logger.debug { "Updating client connection from $clientConnection to $newClientConnection" }
        clientConnection = newClientConnection
    }

    fun send(message: Server2ClientMessage) {
        clientConnection.send(Json.encodeToString(message))
    }

    fun addCloseHandler(handler: () -> Unit) : Int {
        synchronized(this) {
            val id = random.nextInt()
            onCloseHandlers[id] = handler
            return id
        }
    }

    fun removeCloseHandler(id: Int) {
        synchronized(this) {
            onCloseHandlers.remove(id)
        }
    }

    fun triggerCloseListeners() {
        synchronized(this) {
            onCloseHandlers.values.forEach { it() }
            onCloseHandlers.clear()
        }
    }

    override fun toString() = "Remote2ClientState(id = $id)"
}
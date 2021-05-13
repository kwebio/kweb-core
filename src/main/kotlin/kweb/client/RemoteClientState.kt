package kweb.client

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kweb.DebugInfo
import java.time.Instant

data class RemoteClientState(val id: String, @Volatile var clientConnection: ClientConnection,
                             val handlers: MutableMap<Int, (JsonElement) -> Unit> = HashMap(),
                             val debugTokens: MutableMap<String, DebugInfo> = HashMap(), var lastModified: Instant = Instant.now()) {
    fun send(message: Server2ClientMessage) {
        clientConnection.send(Json.encodeToString(message))
    }

    override fun toString() = "Remote2ClientState(id = $id)"
}
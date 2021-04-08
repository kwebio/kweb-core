package kweb.client

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kweb.DebugInfo
import kweb.util.gson
import java.time.Instant
import java.util.*

data class RemoteClientState(val id: String, @Volatile var clientConnection: ClientConnection, val handlers: MutableMap<Int, (Any) -> Unit> = HashMap(), val debugTokens: MutableMap<String, DebugInfo> = HashMap(), var lastModified: Instant = Instant.now()) {
    fun send(message: Server2ClientMessage) {
        clientConnection.send(Json.encodeToString(message))
        //clientConnection.send(gson.toJson(message))
    }

    override fun toString() = "Remote2ClientState(id = $id)"
}
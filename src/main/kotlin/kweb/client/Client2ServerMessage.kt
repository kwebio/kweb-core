package kweb.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

@Serializable
data class Client2ServerMessage(
        val id: String,
        val hello: Boolean? = true,
        val error: ErrorMessage? = null,
        val callback: C2SCallback? = null,
        val historyStateChange: C2SHistoryStateChange? = null
) {

    @Serializable
    data class ErrorMessage(val debugToken: String, val error: Error) {
        @Serializable
        data class Error(val name: String, val message: String)
    }

    @Serializable
    data class C2SCallback(val callbackId: Int, val data: JsonElement = JsonNull)

    @Serializable
    data class C2SHistoryStateChange(val newState: String)
}
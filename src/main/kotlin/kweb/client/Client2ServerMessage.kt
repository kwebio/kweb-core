package kweb.client

data class Client2ServerMessage(
        val id: String,
        val hello: Boolean? = true,
        val error: ErrorMessage? = null,
        val callback: C2SCallback? = null,
        val historyStateChange: C2SHistoryStateChange? = null
) {

    data class ErrorMessage(val debugToken: String, val error: Error) {
        data class Error(val name: String, val message: String)
    }

    data class C2SCallback(val callbackId: Int, val data: Any?)

    data class C2SHistoryStateChange(val newState: String)
}
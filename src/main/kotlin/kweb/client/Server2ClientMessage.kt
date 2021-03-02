package kweb.client

data class Server2ClientMessage(
        val yourId: String,
        val debugToken: String?,
        val jsId: Int? = null,
        val js: String? = null,
        val parameters: String? = null,
        val callbackId: Int? = null,
        val arguments: List<Any?>? = null
)
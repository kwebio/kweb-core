package kweb.client

data class Server2ClientMessage(
        val yourId: String,
        val debugToken: String?,
        val jsId: Int? = null, //the id used to store or receive a js function in cache
        val js: String? = null, //the js function
        val parameters: String? = null, //null if we are executing a cached function
        val callbackId: Int? = null, //null if we are executing a function without a callback
        val arguments: List<Any?>? = null //null if we are executing a function with no arguments
)

//parameters is a comma separated string of parameters for the js function
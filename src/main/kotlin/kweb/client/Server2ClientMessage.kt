package kweb.client

data class Server2ClientMessage(
        val yourId: String,
        val debugToken: String?,
        val jsId: Int,//the int id used to either store or read the cached js function
        val js: String? = null, //the js string to cache, will be null if the function has already been cached
        val parameters: String? = null,
        val arguments: List<Any> = ArrayList<Any>()//a list of arguments to pass to the js function
)

/*
parameters is a comma separated string of the js function parameters,
will be null if already cached or if the function has no parameters
*/
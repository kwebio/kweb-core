package kweb.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.lang.reflect.Constructor

/**
 * Server2client message
 *
 * @property yourId
 * @property debugToken
 * @property jsId The id used to store/retrieve a function from the client's cache. When set to null the client will ignore caching,
 * and simply execute whatever JavaScript code is in this message
 * @property js The Javascript code to execute. Can be null if the function has already been cached.
 * @property parameters A comma separated string of parameter names for the Javascript function
 * @property callbackId The Id used to handle callbacks. Can be null.
 * @property arguments A list of arguments to supply to the Javascript function. The default value is an empty list.
 * When set to null, the client will take the javascript function and cache it, but not actually run/execute the code.
 * This special case is used to cache functions on page render before they are to be called
 */
@Serializable
@SerialName("Server2ClientMessage")
data class Server2ClientMessage(
        val yourId: String,
        val functionCalls : List<FunctionCall>
) {
    constructor(yourId : String, functionCall : FunctionCall) : this(yourId, listOf(functionCall))
    constructor(yourId : String, vararg args : FunctionCall) : this(yourId, listOf(*args))
}

@Serializable
data class FunctionCall(
        var debugToken: String? = null,
        val jsId: Int? = null,
        val js: String? = null,
        val parameters: String? = null,
        val callbackId: Int? = null,
        val arguments: List<JsonElement> = emptyList(),
        var shouldExecute: Boolean = true
)
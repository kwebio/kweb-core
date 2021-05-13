package kweb

import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import kweb.util.KWebDSL
import java.time.Duration

@KWebDSL
class CookieReceiver(val receiver: WebBrowser) {
    fun set(name: String, value: String, expires: Duration? = null, path: String? = null, domain: String? = null) {
        setJson(name, JsonPrimitive(value), expires, path, domain)
    }

    fun set(name: String, value: Int, expires: Duration? = null, path: String? = null, domain: String? = null) {
        setJson(name, JsonPrimitive(value), expires, path, domain)
    }

    fun set(name: String, value: Float, expires: Duration? = null, path: String? = null, domain: String? = null) {
        setJson(name, JsonPrimitive(value), expires, path, domain)
    }

    fun set(name: String, value: Double, expires: Duration? = null, path: String? = null, domain: String? = null) {
        setJson(name, JsonPrimitive(value), expires, path, domain)
    }

    fun set(name: String, value: Short, expires: Duration? = null, path: String? = null, domain: String? = null) {
        setJson(name, JsonPrimitive(value), expires, path, domain)
    }

    fun set(name: String, value: Long, expires: Duration? = null, path: String? = null, domain: String? = null) {
        setJson(name, JsonPrimitive(value), expires, path, domain)
    }

    fun set(name: String, value: Boolean, expires: Duration? = null, path: String? = null, domain: String? = null) {
        setJson(name, JsonPrimitive(value), expires, path, domain)
    }

    fun set(name: String, value: Char, expires: Duration? = null, path: String? = null, domain: String? = null) {
        setJson(name, JsonPrimitive(value.toString()), expires, path, domain)
    }

    fun set(name: String, value: Byte, expires: Duration? = null, path: String? = null, domain: String? = null) {
        setJson(name, JsonPrimitive(value), expires, path, domain)
    }

    //This might have a potential use, I'm not sure.
    fun set(name: String, value: JsonElement, expires: Duration? = null, path: String? = null, domain: String? = null) {
        setJson(name, value, expires, path, domain)
    }

    private fun setJson(name: String, value: JsonElement, expires: Duration? = null, path: String? = null, domain: String? = null) {
        val nameArg = JsonPrimitive(name)
        val expiresArg = if (expires != null) JsonPrimitive(expires.seconds.toString()) else JsonNull
        val pathArg = if (path != null) JsonPrimitive(path) else JsonNull
        val domain = if (domain != null) JsonPrimitive(domain) else JsonNull

        // Calls kweb_bootstrap.js setItem function
        //setItem: function (sKey, sValue, vEnd, sPath, sDomain, bSecure)
        //TODO this setJson function didn't have an argument for bSecure, so I decided to just explicitly pass a JsonNull,
        //Maybe we should add a parameter to the kotlin function for bSecure?
        receiver.callJsFunction("docCookies.setItem({});", nameArg, value, expiresArg, pathArg, domain, JsonNull)

    }

    suspend inline fun <reified V : Any> get(name: String): V? {
        val result = getString(name)
        val serializer = serializer<V>()
        return when(result) {
            null -> null
            else -> Json.decodeFromString(serializer, result)
        }
    }

    /*suspend inline fun <reified V : Any> get(name: String): V? {
        val result = getString(name)
        return when(result) {
            null -> null
            else -> gson.fromJson<V>(result)
        }
    }*/

    suspend fun getString(name: String): String? {
        val result = Json.decodeFromJsonElement<String>(receiver.callJsFunctionWithResult("return docCookies.getItem({});", JsonPrimitive(name)))
        return if (result == "__COOKIE_NOT_FOUND_TOKEN__") {
            null
        } else {
            result
        }
    }

    fun remove(name: String) {
        receiver.callJsFunction("docCookies.removeItem({});", JsonPrimitive(name))
    }
}
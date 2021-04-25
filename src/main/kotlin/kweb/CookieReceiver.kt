package kweb

import com.github.salomonbrys.kotson.fromJson
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kweb.util.KWebDSL
import kweb.util.gson
import java.time.Duration
import java.util.*

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
        val arguments = LinkedList<String>()
        arguments.add(name)
        arguments.add(value.toString())
        if (expires != null) {
            arguments.add(expires.seconds.toString())
        }
        if (path != null) {
            arguments.add(path)
        }
        if (domain != null) {
            arguments.add(domain)
        }

        receiver.callJsFunction("docCookies.setItem({});", JsonPrimitive(arguments.joinToString(separator = ", ")))
    }

    suspend inline fun <reified V : Any> get(name: String): V? {
        val result = getString(name)
        return when(result) {
            null -> null
            else -> gson.fromJson<V>(result)
        }
    }

    suspend fun getString(name: String): String? {
        val result = receiver.callJsFunctionWithResult("return docCookies.getItem({});", JsonPrimitive(name))
        return if (result == "__COOKIE_NOT_FOUND_TOKEN__") {
            null
        } else {
            result.toString()
        }
    }

    fun remove(name: String) {
        receiver.callJsFunction("docCookies.removeItem({});", JsonPrimitive(name))
    }
}
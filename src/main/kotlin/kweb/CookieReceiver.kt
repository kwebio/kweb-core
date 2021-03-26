package kweb

import com.github.salomonbrys.kotson.fromJson
import kweb.util.KWebDSL
import kweb.util.gson
import kweb.util.toJson
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture

@KWebDSL
class CookieReceiver(val receiver: WebBrowser) {
    fun set(name: String, value: Any, expires: Duration? = null, path: String? = null, domain: String? = null) {
        setString(name, value.toJson(), expires, path, domain)
    }

    fun setString(name: String, value: String, expires: Duration? = null, path: String? = null, domain: String? = null) {
        val arguments = LinkedList<String>()
        arguments.add(name.toJson())
        arguments.add(value.toJson())
        if (expires != null) {
            arguments.add(expires.seconds.toString())
        }
        if (path != null) {
            arguments.add(path)
        }
        if (domain != null) {
            arguments.add(domain)
        }

        receiver.callJsFunction("docCookies.setItem({});", arguments.joinToString(separator = ", "))
    }

    suspend inline fun <reified V : Any> get(name: String): V? {
        val result = getString(name)
        return when(result) {
            null -> null
            else -> gson.fromJson<V>(result)
        }
    }

    suspend fun getString(name: String): String? {
        val result = receiver.callJsFunctionWithResult("return docCookies.getItem({});", name.toJson())
        return if (result == "__COOKIE_NOT_FOUND_TOKEN__") {
            null
        } else {
            result.toString()
        }
    }

    fun remove(name: String) {
        receiver.callJsFunction("docCookies.removeItem({});", name.toJson())
    }
}
package io.kweb.dom.cookies

import com.github.salomonbrys.kotson.fromJson
import io.kweb.WebBrowser
import io.kweb.dom.element.KWebDSL
import io.kweb.gson
import io.kweb.toJson
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

        receiver.execute("docCookies.setItem(${arguments.joinToString(separator = ", ")});")
    }

    inline fun <reified V : Any> get(name: String): CompletableFuture<V?> = getString(name).thenApply {
        when (it) {
            null -> null
            else -> gson.fromJson<V>(it)
        }
    }

    fun getString(name: String): CompletableFuture<String?> {
        return receiver.evaluate("docCookies.getItem(${name.toJson()});")
                .thenApply {
                    if (it == "__COOKIE_NOT_FOUND_TOKEN__") {
                        null
                    } else {
                        it
                    }
                }
    }

    fun remove(name: String) {
        receiver.execute("docCookies.removeItem(${name.toJson()});")
    }
}
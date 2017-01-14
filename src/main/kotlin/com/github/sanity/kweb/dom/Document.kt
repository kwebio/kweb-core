package com.github.sanity.kweb.dom

import com.github.sanity.kweb.RootReceiver
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.toJson
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture

class Document(private val receiver: RootReceiver) {
    fun getElementById(id: String) = Element(receiver, "document.getElementById(\"$id\")")

    val cookie = CookieReceiver(receiver)

    val body = Element(receiver, "document.body")
}

class CookieReceiver(val receiver: RootReceiver) {
    fun set(name: String, value: String, expires: Duration? = null, path: String? = null, domain: String? = null) {
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

    inline fun get(name: String): CompletableFuture<String?> {
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

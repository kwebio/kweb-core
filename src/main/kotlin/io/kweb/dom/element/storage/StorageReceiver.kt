package io.kweb.dom.element.storage

import com.github.salomonbrys.kotson.fromJson
import io.kweb.WebBrowser
import io.kweb.dom.element.KWebDSL
import io.kweb.gson
import io.kweb.toJson
import java.util.concurrent.CompletableFuture

/**
 * Created by ian on 1/14/17.
 */
@KWebDSL
class StorageReceiver(val receiver: WebBrowser, val type: StorageType) {
    private val obj = "${type.name}Storage"

    operator fun set(name: String, value: Any) {
        setString(name, value.toJson())
    }

    fun setString(key: String, value: String) {
        if (value == "") {
            throw IllegalArgumentException("$obj cannot store the value \"\"")
        }
        receiver.execute("$obj.setItem(${key.toJson()}, ${value.toJson()});")
    }

    operator inline fun <reified V : Any> get(name: String): CompletableFuture<V?> = getString(name).thenApply {
        when (it) {
            null -> null
            else -> gson.fromJson<V>(it)
        }
    }

    fun getString(key: String): CompletableFuture<String?> = receiver.evaluate("$obj.getItem(${key.toJson()})").thenApply {
        when (it) {
            "" -> null
            else -> it
        }
    }

    fun remove(key: String) {
        receiver.execute("$obj.removeItem(${key.toJson()});")

    }

}

enum class StorageType {
    local, session
}

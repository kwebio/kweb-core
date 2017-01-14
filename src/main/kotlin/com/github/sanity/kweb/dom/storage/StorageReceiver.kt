package com.github.sanity.kweb.dom.storage

import com.github.salomonbrys.kotson.fromJson
import com.github.sanity.kweb.RootReceiver
import com.github.sanity.kweb.gson
import com.github.sanity.kweb.toJson
import java.util.concurrent.CompletableFuture

/**
 * Created by ian on 1/14/17.
 */
class StorageReceiver(val receiver: RootReceiver, val type: StorageType) {
    private val obj = "${type.name}Storage"

    fun set(name: String, value: Any) {
        setString(name, value.toJson())
    }

    fun setString(key: String, value: String) {
        if (value == "") {
            throw IllegalArgumentException("$obj cannot store the value \"\"")
        }
        receiver.execute("$obj.setItem(${key.toJson()}, ${value.toJson()});")
    }

    inline fun <reified V : Any> get(name: String): CompletableFuture<V?> = getString(name).thenApply {
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

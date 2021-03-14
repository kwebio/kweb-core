package kweb.html

import com.github.salomonbrys.kotson.fromJson
import kweb.WebBrowser
import kweb.util.KWebDSL
import kweb.util.gson
import kweb.util.toJson
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
        receiver.callJsFunction("$obj.setItem(${key.toJson()}, ${value.toJson()});")
    }

    inline operator fun <reified V : Any> get(name: String): CompletableFuture<V?> = getString(name).thenApply {
        when (it) {
            null -> null
            else -> gson.fromJson<V>(it)
        }
    }

    fun getString(key: String): CompletableFuture<String?> = receiver.callJsFunctionWithResult("return $obj.getItem(${key.toJson()})").thenApply {
        when (it) {
            "" -> null
            else -> it.toString()
        }
    }

    fun remove(key: String) {
        receiver.callJsFunction("$obj.removeItem(${key.toJson()});")

    }

}

enum class StorageType {
    local, session
}

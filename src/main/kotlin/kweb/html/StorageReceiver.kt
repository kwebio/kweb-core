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
        receiver.callJsFunction("{}.setItem({}, {});", obj, key.toJson(), value.toJson())
    }

    suspend inline fun <reified V : Any> get(name: String): V? {
        val result = getString(name)
        return when(result) {
            null -> null
            else -> gson.fromJson<V>(result)
        }
    }

    suspend fun getString(key: String): String? {
        val result = receiver.callJsFunctionWithResult("return $obj.getItem({});", key.toJson())
        return when (result) {
            "" -> null
            else -> result.toString()
        }
    }

    fun remove(key: String) {
        receiver.callJsFunction("{}.removeItem({});", obj, key.toJson())
    }

}

enum class StorageType {
    local, session
}

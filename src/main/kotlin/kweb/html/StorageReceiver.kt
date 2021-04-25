package kweb.html

import com.github.salomonbrys.kotson.fromJson
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kweb.WebBrowser
import kweb.util.KWebDSL
import kweb.util.gson

/**
 * Created by ian on 1/14/17.
 */
@KWebDSL
class StorageReceiver(val receiver: WebBrowser, val type: StorageType) {
    private val obj = "${type.name}Storage"

    operator fun set(name: String, value: String) {
        setJson(name, JsonPrimitive(value))
    }

    operator fun set(name: String, value: Int) {
        setJson(name, JsonPrimitive(value))
    }

    operator fun set(name: String, value: Float) {
        setJson(name, JsonPrimitive(value))
    }

    operator fun set(name: String, value: Double) {
        setJson(name, JsonPrimitive(value))
    }

    operator fun set(name: String, value: Short) {
        setJson(name, JsonPrimitive(value))
    }

    operator fun set(name: String, value: Long) {
        setJson(name, JsonPrimitive(value))
    }

    operator fun set(name: String, value: Boolean) {
        setJson(name, JsonPrimitive(value))
    }

    operator fun set(name: String, value: Char) {
        setJson(name, JsonPrimitive(value.toString()))
    }

    operator fun set(name: String, value: Byte) {
        setJson(name, JsonPrimitive(value))
    }

    //I don't know if we actually need this one, but it seems like it might be useful at some point.
    operator fun set(name: String, value: JsonElement) {
        setJson(name, value)
    }

    fun setJson(key: String, value: JsonElement) {
        if (value == JsonNull || value.toString() == "") {
            throw IllegalArgumentException("$obj cannot store the value \"\"")
        }
        receiver.callJsFunction("$obj.setItem({}, {});", JsonPrimitive(key), value)
    }

    suspend inline fun <reified V : Any> get(name: String): V? {
        val result = getString(name)
        return when(result) {
            null -> null
            //TODO gson usage
            else -> gson.fromJson<V>(result)
        }
    }

    suspend fun getString(key: String): String? {
        val result = receiver.callJsFunctionWithResult("return $obj.getItem({});", JsonPrimitive(key))
        return when (result) {
            "" -> null
            else -> result.toString()
        }
    }

    fun remove(key: String) {
        receiver.callJsFunction("$obj.removeItem({});", JsonPrimitive(key))
    }

}

enum class StorageType {
    local, session
}

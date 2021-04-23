package kweb.html

import com.github.salomonbrys.kotson.fromJson
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kweb.WebBrowser
import kweb.util.KWebDSL
import kweb.util.gson
import kweb.util.toJson

/**
 * Created by ian on 1/14/17.
 */
@KWebDSL
class StorageReceiver(val receiver: WebBrowser, val type: StorageType) {
    private val obj = "${type.name}Storage"

    operator fun set(name: String, value: String) {
        setString(name, value)
    }

    operator fun set(name: String, value: Int) {
        setString(name, value.toString())
    }

    operator fun set(name: String, value: Float) {
        setString(name, value.toString())
    }

    operator fun set(name: String, value: Double) {
        setString(name, value.toString())
    }

    operator fun set(name: String, value: Short) {
        setString(name, value.toString())
    }

    operator fun set(name: String, value: Long) {
        setString(name, value.toString())
    }

    operator fun set(name: String, value: Boolean) {
        setString(name, value.toString())
    }

    operator fun set(name: String, value: Char) {
        setString(name, value.toString())
    }

    operator fun set(name: String, value: Byte) {
        setString(name, value.toString())
    }

    //I don't know if we actually need this one, but it seems like it might be useful at some point.
    operator fun set(name: String, value: JsonElement) {
        setString(name, value.toString())
    }

    fun setString(key: String, value: String) {
        if (value == "") {
            throw IllegalArgumentException("$obj cannot store the value \"\"")
        }
        receiver.callJsFunction("$obj.setItem({}, {});", JsonPrimitive(key), JsonPrimitive(value))
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
        val result = receiver.callJsFunctionWithResult("return $obj.getItem({});", key.toJson())
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

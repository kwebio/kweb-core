package kweb.html

import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import kweb.WebBrowser
import kweb.util.KWebDSL

/**
 * Created by ian on 1/14/17.
 */
@KWebDSL
class StorageReceiver(val receiver: WebBrowser, val type: StorageType) {
    private val obj = "${type.name}Storage"

    operator fun set(key: String, value: String) {
        set(key, JsonPrimitive(value))
    }

    operator fun set(key: String, value: Int) {
        set(key, JsonPrimitive(value))
    }

    operator fun set(key: String, value: Float) {
        set(key, JsonPrimitive(value))
    }

    operator fun set(key: String, value: Double) {
        set(key, JsonPrimitive(value))
    }

    operator fun set(key: String, value: Short) {
        set(key, JsonPrimitive(value))
    }

    operator fun set(key: String, value: Long) {
        set(key, JsonPrimitive(value))
    }

    operator fun set(key: String, value: Boolean) {
        set(key, JsonPrimitive(value))
    }

    operator fun set(key: String, value: Char) {
        set(key, JsonPrimitive(value.toString()))
    }

    operator fun set(key: String, value: Byte) {
        set(key, JsonPrimitive(value))
    }

    operator fun set(key: String, value: JsonElement) {
        if (value == JsonNull || value.toString() == "") {
            throw IllegalArgumentException("$obj cannot store the value \"\"")
        }
        receiver.callJsFunction("$obj.setItem({}, {});", JsonPrimitive(key), value)
    }

    suspend inline fun <reified V : Any> get(name: String): V? {
        val result = getString(name)
        val serializer = serializer<V>()
        return when(result) {
            null -> null
            else -> Json.decodeFromString(serializer, result)
        }
    }

    suspend fun getString(key: String): String? {
        val result = Json.decodeFromJsonElement<String>(receiver.callJsFunctionWithResult("return $obj.getItem({});", JsonPrimitive(key)))
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

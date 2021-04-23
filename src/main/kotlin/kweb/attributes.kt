package kweb

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Created by ian on 1/14/17.
 */

val attr: MutableMap<String, JsonElement> get() = AttributeBuilder()

open class AttributeBuilder : MutableMap<String, JsonElement> by LinkedHashMap()

fun Map<String, JsonElement>.set(key: String, value: JsonElement): Map<String, JsonElement> {
    if (value != JsonNull) {
        if (this is AttributeBuilder) {
            put(key, value)
            return this
        } else {
            val clonedAttributes = AttributeBuilder()
            clonedAttributes.putAll(this)
            clonedAttributes.put(key, value)
            return clonedAttributes
        }
    } else {
        return this
    }
}

fun Map<String, JsonElement>.id(id: String): Map<String, JsonElement> = set("id", JsonPrimitive(id))

fun Map<String, JsonElement>.classes(classes: Iterable<String>, condition: Boolean = true): Map<String, JsonElement> {
    if (condition) {
        val classAttributeValue = get("class").toString()
        val existing: List<String> = when (classAttributeValue) {
            is String -> classAttributeValue.split(' ')
            else -> listOf()
        }
        // TODO: This is inefficient when classes() is called multiple times
        return set("class", JsonPrimitive((existing + classes).joinToString(separator = " ")))
    } else {
        return this
    }
}

val Map<String, JsonElement>.disabled get() = this.set("disabled", JsonPrimitive(true))

fun Map<String, JsonElement>.classes(vararg classes: String, onlyIf: Boolean = true): Map<String, Any> = classes(classes.asIterable(), onlyIf)
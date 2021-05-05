package kweb

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonNull

/**
 * Created by ian on 1/14/17.
 */

val attr: MutableMap<String, JsonPrimitive> get() = AttributeBuilder()

open class AttributeBuilder : MutableMap<String, JsonPrimitive> by LinkedHashMap()

fun Map<String, JsonPrimitive>.set(key: String, value: JsonPrimitive): Map<String, JsonPrimitive> {
    if (value != JsonNull) {
        if (this is AttributeBuilder) {
            println("AtributeBuilder is setting $key as $value")
            put(key, value)
            return this
        } else {
            val clonedAttributes = AttributeBuilder()
            clonedAttributes.putAll(this)
            clonedAttributes.put(key, JsonPrimitive(value.content))
            println("ClonedAttributes is setting $key as $value")
            return clonedAttributes
        }
    } else {
        return this
    }
}

fun Map<String, JsonPrimitive>.id(id: String): Map<String, JsonPrimitive> = set("id", JsonPrimitive(id))

fun Map<String, JsonPrimitive>.classes(classes: Iterable<String>, condition: Boolean = true): Map<String, JsonPrimitive> {
    println(this.entries)
    if (condition) {
        var existing: List<String>? = null
        val classAttributeValue = get("class").toString()
        if (classAttributeValue != "null") {
            println("classAttributeValue = $classAttributeValue")
            existing = classAttributeValue.split(' ')
        } else {
            println("classAttribueValue was null")
            existing = null
        }
        /*existing = when (classAttributeValue) {
            "" -> listOf()
            is String -> classAttributeValue.split(' ')
            else -> listOf()
        }*/
        println("existing = $existing")
        // TODO: This is inefficient when classes() is called multiple times
        val classString = if (existing != null) {
            JsonPrimitive((existing + classes).joinToString(separator = " "))
        } else {
            JsonPrimitive(classes.joinToString(separator = ""))
        }
        println("Setting class as $classString")
        return set("class", classString)
    } else {
        return this
    }
}

val Map<String, JsonPrimitive>.disabled get() = this.set("disabled", JsonPrimitive(true))

fun Map<String, JsonPrimitive>.classes(vararg classes: String, onlyIf: Boolean = true): Map<String, JsonPrimitive> = classes(classes.asIterable(), onlyIf)
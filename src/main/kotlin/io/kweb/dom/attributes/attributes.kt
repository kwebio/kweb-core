package io.kweb.dom.attributes

/**
 * Created by ian on 1/14/17.
 */

val attr: MutableMap<String, Any> get() = AttributeBuilder()

open class AttributeBuilder : MutableMap<String, Any> by LinkedHashMap()

fun Map<String, Any>.set(key : String, value : Any?) : Map<String, Any> {
    if (value != null) {
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

fun Map<String, Any>.id(id: String): Map<String, Any> = set("id", id)

fun Map<String, Any>.classes(classes: Iterable<String>, condition: Boolean = true): Map<String, Any> {
    if (condition) {
        val classAttributeValue = get("class")
        val existing: List<String> = when (classAttributeValue) {
            is String -> classAttributeValue.split(' ')
            else -> listOf()
        }
        // TODO: This is inefficient when classes() is called multiple times
        return set("class", (existing + classes).joinToString(separator = " "))
    } else {
        return this
    }
}

val Map<String, Any>.disabled get() = this.set("disabled", true)

fun Map<String, Any>.classes(vararg classes: String, onlyIf: Boolean = true): Map<String, Any> = classes(classes.asIterable(), onlyIf)
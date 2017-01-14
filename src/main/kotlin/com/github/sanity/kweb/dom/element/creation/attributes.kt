package com.github.sanity.kweb.dom.element.creation

/**
 * Created by ian on 1/14/17.
 */

val attr: MutableMap<String, Any> get() = AttributeBuilder()

private class AttributeBuilder : MutableMap<String, Any> by LinkedHashMap()

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

fun Map<String, Any>.classes(vararg classes: String): Map<String, Any> {
    val classAttributeValue = get("class")
    val existing: List<String> = when (classAttributeValue) {
        is String -> classAttributeValue.split(' ')
        else -> listOf()
    }
    return set("class", (existing + classes).joinToString(separator = " "))
}
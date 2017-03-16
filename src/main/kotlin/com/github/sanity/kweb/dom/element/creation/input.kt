package com.github.sanity.kweb.dom.element.creation

import com.github.salomonbrys.kotson.toJson
import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.set
import com.github.sanity.kweb.dom.element.Element
import java.util.concurrent.CompletableFuture

fun Element.input(type: InputType? = null, name: String? = null, initialValue: String? = null, size: Int? = null, attributes: Map<String, Any> = attr): InputElement {
    return InputElement(createElement("input", attributes = attributes
            .set("type", type?.name)
            .set("name", name)
            .set("value", initialValue)
            .set("size", size)
    ))
}


open class InputElement(val element: Element) : Element(element) {
    fun getValue(): CompletableFuture<String> = element.evaluate("$jsExpression.value", { s: String -> s }) ?: throw RuntimeException("Not sure why .evaluate() would return null")
    fun setValue(newValue: String) = element.rootReceiver.execute("$jsExpression.value=${newValue.toJson()}")
}

enum class InputType {
    button, checkbox, color, date, datetime, email, file, hidden, image, month, number, password, radio, range, reset, search, submit, tel, text, time, url, week
}

fun Element.label(attributes: Map<String, Any> = attr, position : Int? = null) = LabelElement(createElement("label", attributes = attributes, position = position))

open class LabelElement(wrapped: Element) : Element(wrapped)

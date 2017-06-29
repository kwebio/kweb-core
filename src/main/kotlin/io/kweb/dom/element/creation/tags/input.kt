package io.kweb.dom.element.creation.tags

import com.github.salomonbrys.kotson.toJson
import io.kweb.dom.attributes.attr
import io.kweb.dom.attributes.set
import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.ElementCreator
import java.util.concurrent.CompletableFuture

/**
 *
 */

fun ElementCreator<Element>.input(type: InputType? = null, name: String? = null, initialValue: String? = null, size: Int? = null, placeholder : String? = null, attributes: Map<String, Any> = attr): InputElement {
    return InputElement(element("input", attributes = attributes
            .set("type", type?.name)
            .set("name", name)
            .set("value", initialValue)
            .set("placeholder", placeholder)
            .set("size", size)
    ))
}


open class InputElement(val element: Element) : Element(element) {
    fun checked(checked : Boolean = false)  = setAttribute("checked", checked)
    fun getValue(): CompletableFuture<String> = element.evaluate("$jsExpression.value", { s: String -> s }) ?: throw RuntimeException("Not sure why .evaluate() would return null")
    fun setValue(newValue: String) = element.webBrowser.execute("$jsExpression.value=${newValue.toJson()}")
}

enum class InputType {
    button, checkbox, color, date, datetime, email, file, hidden, image, month, number, password, radio, range, reset, search, submit, tel, text, time, url, week
}

fun ElementCreator<Element>.label(forId: String?, attributes: Map<String, Any> = attr) = LabelElement(element("label", attributes = attributes.set("for", forId)))

fun ElementCreator<Element>.label(for_: Element? = null, attributes: Map<String, Any> = attr) = LabelElement(element("label", attributes = attributes.set("for", for_?.id)))

open class LabelElement(wrapped: Element) : Element(wrapped)

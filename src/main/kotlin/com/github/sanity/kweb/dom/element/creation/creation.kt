package com.github.sanity.kweb.dom.element.creation

import com.github.salomonbrys.kotson.toJson
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.read.ElementReader
import com.github.sanity.kweb.dom.element.modification.setText
import com.github.sanity.kweb.random
import com.github.sanity.kweb.toJson
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Created by ian on 1/13/17.
 */


/*********
 ********* Element creation functions.
 *********
 ********* These allow creation of element types as children of the current element.
 ********* With the exception of createElement(), they do not begin with verbs, and
 ********* will typically be just the tag of the element like "div" or "input".
 *********/

fun Element.createElement(tag: String, attributes: Map<String, Any> = Collections.emptyMap()): Element {
    val id = attributes["id"] ?: Math.abs(random.nextInt()).toString()
    val javaScript = StringBuilder()
    with(javaScript) {
        appendln("{")
        appendln("var newEl = document.createElement(\"$tag\");")
        if (!attributes.containsKey("id")) {
            appendln("newEl.setAttribute(\"id\", \"$id\");")
        }
        for ((name, value) in attributes) {
            appendln("newEl.setAttribute(\"$name\", ${value.toJson()});")
        }
        appendln("$jsExpression.appendChild(newEl);")
        appendln("}")
    }
    execute(javaScript.toString())
    return Element(receiver, "document.getElementById(\"$id\")")
}

fun Element.div(attributes: Map<String, Any> = Collections.emptyMap()) = DIVElement(createElement("div", attributes))

class DIVElement(wrapped: Element) : Element(wrapped.receiver, wrapped.jsExpression) {
}

fun Element.h1(text: String, attributes: Map<String, String> = Collections.emptyMap()): Element {
    return createElement("h1", attributes).setText(text)
}

fun Element.p(text: String, attributes: Map<String, String> = Collections.emptyMap()): Element {
    return createElement("p", attributes).setText(text)
}

fun Element.ul(attributes: Map<String, String> = Collections.emptyMap()): ULElement {
    val e = createElement("ul", attributes)
    return ULElement(e)
}

class ULElement(wrapped: Element) : Element(wrapped.receiver, wrapped.jsExpression) {
    fun li(attributes: Map<String, String> = Collections.emptyMap()) = createElement("li", attributes)
}

fun Element.input(type: InputType? = null, name: String? = null, initialValue: String? = null, size: Int? = null): InputElement {
    val attributes = HashMap<String, Any>()
    if (type != null) attributes.put("type", type.name)
    if (name != null) attributes.put("name", name)
    if (initialValue != null) attributes.put("value", initialValue)
    if (size != null) attributes.put("size", size)
    return InputElement(createElement("input", attributes))
}

class InputElement(val element: Element) : Element(element.receiver, element.jsExpression) {
    fun getValue(): CompletableFuture<String> = element.evaluate("$jsExpression.value", { s: String -> s }) ?: throw RuntimeException("Not sure why .evaluate() would return null")
    fun setValue(newValue: String) = element.receiver.execute("$jsExpression.value=${newValue.toJson()}")
}

enum class InputType {
    button, checkbox, color, date, datetime, email, file, hidden, image, month, number, password, radio, range, reset, search, submit, tel, text, time, url, week
}

fun Element.button(type: ButtonType = ButtonType.button, autofocus: Boolean? = null, disabled: Boolean? = null): Element {
    val attributes = HashMap<String, Any>()
    attributes.put("type", type.name)
    if (autofocus != null) attributes.put("autofocus", autofocus)
    if (disabled != null) attributes.put("disabled", disabled)
    return createElement("button", attributes)
}

enum class ButtonType {
    button, reset, submit
}

fun Element.nav(attributes: Map<String, String> = Collections.emptyMap()): NavElement {
    return NavElement(createElement("nav", attributes))
}

class NavElement(element: Element) : Element(element.receiver, element.jsExpression) {

}

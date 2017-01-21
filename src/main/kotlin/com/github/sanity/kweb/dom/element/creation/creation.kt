package com.github.sanity.kweb.dom.element.creation

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.random
import com.github.sanity.kweb.toJson
import java.util.*

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

fun Element.createElement(tag: String, attributes: Map<String, Any> = HashMap()): Element {
    val id: String = (attributes["id"] ?: Math.abs(random.nextInt())).toString()
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
    return Element(receiver, jsExpression = "document.getElementById(\"$id\")", id = id)
}

fun Element.div(attributes: Map<String, Any> = HashMap()) = DIVElement(createElement("div", attributes))

open class DIVElement(wrapped: Element) : Element(wrapped) {
    // These are useful to attach extension functions to
}


fun Element.span(attributes: Map<String, Any> = HashMap()) = SpanElement(createElement("span", attributes))

open class SpanElement(wrapped: Element) : Element(wrapped) {
    // These are useful to attach extension functions to
}

fun Element.main(attributes: Map<String, Any> = HashMap()) = MainElement(createElement("main", attributes))

open class MainElement(wrapped: Element) : Element(wrapped) {
    // These are useful to attach extension functions to
}

fun Element.h1(attributes: Map<String, Any> = HashMap()): Element {
    return createElement("h1", attributes)
}

fun Element.a(href : String? = null, attributes: Map<String, Any> = HashMap()): Element {
    return createElement("a", attributes.set("href", href))
}

fun Element.p(attributes: Map<String, Any> = HashMap()): Element {
    return createElement("p", attributes)
}

fun Element.ul(attributes: Map<String, Any> = HashMap()): ULElement {
    val e = createElement("ul", attributes)
    return ULElement(e)
}

fun Element.form(action: String? = null, attributes: Map<String, Any> = HashMap()): Element {
    return createElement("form", attributes.set("action", action))
}

class ULElement(wrapped: Element) : Element(wrapped) {
    fun li(attributes: Map<String, Any> = HashMap()) = createElement("li", attributes)
}

fun Element.button(type: ButtonType? = ButtonType.button, autofocus: Boolean? = null, attributes: Map<String, Any> = attr): ButtonElement {
    return ButtonElement(createElement("button", attributes
            .set("type", type?.name)
            .set("autofocus", autofocus)
    ))
}

class ButtonElement(val wrapped: Element) : Element(wrapped) {

}

enum class ButtonType {
    button, reset, submit
}

fun Element.nav(attributes: Map<String, Any> = HashMap()): NavElement {
    return NavElement(createElement("nav", attributes))
}

class NavElement(element: Element) : Element(element) {

}

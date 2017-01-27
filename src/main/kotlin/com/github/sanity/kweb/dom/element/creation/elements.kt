package com.github.sanity.kweb.dom.element.creation

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.set
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.random
import com.github.sanity.kweb.toJson

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

fun Element.createElement(tag: String, attributes: Map<String, Any> = attr): Element {
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
    return Element(receiver, tag = tag, jsExpression = "document.getElementById(\"$id\")", id = id)
}

fun Element.div(attributes: Map<String, Any> = attr) = DivElement(createElement("div", attributes))

open class DivElement(wrapped: Element) : Element(wrapped) {
    // These are useful to attach extension functions to
}


fun Element.span(attributes: Map<String, Any> = attr) = SpanElement(createElement("span", attributes))

open class SpanElement(wrapped: Element) : Element(wrapped) {
}

fun Element.main(attributes: Map<String, Any> = attr) = MainElement(createElement("main", attributes))

open class MainElement(wrapped: Element) : Element(wrapped) {
    // These are useful to attach extension functions to
}

fun Element.h1(attributes: Map<String, Any> = attr): Element {
    return createElement("h1", attributes)
}

fun Element.a(href: String? = null, attributes: Map<String, Any> = attr): Element {
    return createElement("a", attributes.set("href", href))
}

fun Element.p(attributes: Map<String, Any> = attr): Element {
    return createElement("p", attributes)
}

fun Element.ul(attributes: Map<String, Any> = attr): ULElement {
    val e = createElement("ul", attributes)
    return ULElement(e)
}

fun Element.i(attributes: Map<String, Any> = attr) = IElement(createElement("i", attributes))

open class IElement(wrapped : Element) : Element(wrapped)

fun Element.form(action: String? = null, method: String? = null, attributes: Map<String, Any> = attr): Element {
    return createElement("form", attributes
            .set("action", action)
            .set("method", method)
    )
}

open class FormElement(wrapped: Element) : Element(wrapped)

fun Element.header(attributes: Map<String, Any> = attr) = HeaderElement(createElement("header", attributes))

open class HeaderElement(wrapped: Element) : Element(wrapped)

fun Element.footer(attributes: Map<String, Any> = attr) = FooterElement(createElement("footer", attributes))

open class FooterElement(wrapped: Element) : Element(wrapped)

open class ULElement(wrapped: Element) : Element(wrapped) {
    open fun li(attributes: Map<String, Any> = attr) = LIElement(createElement("item", attributes))
}

open class LIElement(wrapped : Element) : Element(wrapped) {

}

fun Element.nav(attributes: Map<String, Any> = attr): NavElement {
    return NavElement(createElement("nav", attributes))
}

open class NavElement(element: Element) : Element(element) {

}

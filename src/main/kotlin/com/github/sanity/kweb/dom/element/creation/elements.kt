package com.github.sanity.kweb.dom.element.creation

import com.github.salomonbrys.kotson.toJson
import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.set
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.modification.setAttribute
import com.github.sanity.kweb.dom.element.modification.setText
import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.random
import com.github.sanity.kweb.toJson
import mu.KLogging
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

/**
 * Created by ian on 1/13/17.
 */


/*********
 ********* Element creation functions.
 *********
 ********* These allow creation of parent types as children of the current parent.
 ********* With the exception of element(), they do not begin with verbs, and
 ********* will typically be just the tag of the parent like "div" or "input".
 *********/

fun Element.insert(position : Int? = null) = ElementCreator(this, position)

open class ElementCreator(val parent : Element, val position : Int? = null) {
    companion object: KLogging()

    var elementCreationCount = 0

    fun element(tag: String, attributes: Map<String, Any> = attr): Element {
        elementCreationCount++
        if (position != null && elementCreationCount == 2) {
            logger.warn {
                """
                It's unwise to element multiple elements using the same ElementCreator when position is specified,
                because each element will be added at the same position among its siblings, which will result in them
                being inserted in reverse-order.
                """.trimIndent().trim() }
        }

        val id: String = (attributes["id"] ?: Math.abs(random.nextInt())).toString()
        val javaScript = StringBuilder()
        with(javaScript) {
            appendln("{")
            appendln("var newEl = document.element(\"$tag\");")
            if (!attributes.containsKey("id")) {
                appendln("newEl.setAttribute(\"id\", \"$id\");")
            }
            for ((name, value) in attributes) {
                appendln("newEl.setAttribute(\"$name\", ${value.toJson()});")
            }
            if (position == null) {
                appendln("${parent.jsExpression}.appendChild(newEl);")
            } else {
                appendln("${parent.jsExpression}.insertBefore(newEl, ${parent.jsExpression}.childNodes[$position]);")
            }
            appendln("}")
        }
        parent.execute(javaScript.toString())
        return Element(parent.rootReceiver, tag = tag, jsExpression = "document.getElementById(\"$id\")", id = id)
    }

    fun require(vararg plugins: KClass<out KWebPlugin>) = parent.rootReceiver.require(*plugins)

    fun div(attributes: Map<String, Any> = attr, position : Int? = null) = DivCreator(element("div", attributes))

    fun span(attributes: Map<String, Any> = attr, position : Int? = null) = SpanCreator(element("span", attributes))

    fun main(attributes: Map<String, Any> = attr, position : Int? = null) = MainCreator(element("main", attributes))

    fun h1(attributes: Map<String, Any> = attr): Element = element("h1", attributes)

    fun a(href: String? = null, attributes: Map<String, Any> = attr): ACreator = ACreator(element("a", attributes.set("href", href)))

    fun p(attributes: Map<String, Any> = attr): Element = element("p", attributes)

    fun ul(attributes: Map<String, Any> = attr): ULCreator {
        val e = element("ul", attributes)
        return ULCreator(e)
    }

    fun text(text : String) : ElementCreator {
        parent.setText(text)
        return this
    }

    fun i(attributes: Map<String, Any> = attr, position : Int? = null) = ICreator(element("i", attributes))
    open class ICreator(wrapped : Element) : ElementCreator(wrapped)

    fun form(position : Int? = null, action: String? = null, method: String? = null, attributes: Map<String, Any> = attr): Element {
        return element("form", attributes
                .set("action", action)
                .set("method", method)
        )
    }

    fun select(attributes: Map<String, Any> = attr) = SelectCreator(element("select", attributes))

    fun header(attributes: Map<String, Any> = attr, position : Int? = null) = HeaderCreator(element("header", attributes))

    fun footer(attributes: Map<String, Any> = attr) = ElementCreator(element("footer", attributes))

    fun nav(attributes: Map<String, Any> = attr): NavCreator {
        return NavCreator(element("nav", attributes))
    }

}

open class ACreator(parent : Element) : ElementCreator(parent)
open class HeaderCreator(parent: Element) : ElementCreator(parent)
open class DivCreator(parent: Element) : ElementCreator(parent)
open class SpanCreator(parent: Element) : ElementCreator(parent)
open class MainCreator(parent: Element) : ElementCreator(parent)
open class ULCreator(parent: Element) : ElementCreator(parent) {
    open fun li(attributes: Map<String, Any> = attr, position : Int? = null) = LICreator(parent.insert().element("item", attributes))
}
open class LICreator(wrapped : Element) : ElementCreator(wrapped)
open class SelectCreator(val wrapped : Element) : ElementCreator(wrapped) {
    fun option(value : String, attributes: Map<String, Any> = attr)
            = element("option").setAttribute("value", value.toJson())

    fun optgroup(label : String, attributes: Map<String, Any> = attr)
            = OptGroup(element("optgroup").setAttribute("label", label.toJson()))

    fun getValue(): CompletableFuture<String>? = wrapped.evaluate("${wrapped.jsExpression}.value", {it})
    fun  setValue(value: String) = wrapped.execute("${wrapped.jsExpression}.value = ${value.toJson()};")
}
open class FormElement(wrapped: Element) : Element(wrapped)
open class OptGroup(wrapped: Element) : Element(wrapped) {
    fun option(value : String, attributes: Map<String, Any> = attr)
            = insert().element("option").setAttribute("value", value.toJson())
}
open class NavCreator(element: Element) : ElementCreator(element)









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


// these should return Element, not ElementCreators.  If they want to create more
// within a dsl let them type element.create().ul()...


open class ElementCreator<out PARENT_TYPE : Element>(val parent: PARENT_TYPE, val position : Int? = null) {

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

    fun div(attributes: Map<String, Any> = attr, position : Int? = null) = element("div", attributes)

    class DivElement(val element : Element) : Element(element) {
        override fun create(position : Int?) = ElementCreator<DivElement>(this)
    }

    fun span(attributes: Map<String, Any> = attr, position : Int? = null) = SpanElement(element("span", attributes))

    open class SpanElement(element: Element) : Element(element) {
        override fun create(position: Int?) = ElementCreator(this)
    }

    fun main(attributes: Map<String, Any> = attr, position : Int? = null) = element("main", attributes)

    fun h1(attributes: Map<String, Any> = attr) = element("h1", attributes)

    fun a(href: String? = null, attributes: Map<String, Any> = attr) = element("a", attributes.set("href", href))

    fun p(attributes: Map<String, Any> = attr) = element("p", attributes)
    fun ul(attributes: Map<String, Any> = attr) = ULElement(element("ul", attributes))

    fun text(text : String) {
        parent.setText(text)
    }

    fun i(attributes: Map<String, Any> = attr, position : Int? = null) = ICreator(element("i", attributes))
    open class ICreator(wrapped : Element) : ElementCreator(wrapped)

    fun form(position : Int? = null, action: String? = null, method: String? = null, attributes: Map<String, Any> = attr): Element {
        return element("form", attributes
                .set("action", action)
                .set("method", method)
        )
    }

    fun select(attributes: Map<String, Any> = attr) = SelectElement(element("select", attributes))

    class SelectElement(val parent : Element) : Element(parent) {
        override fun create(position: Int?): SelectCreator {
            return SelectCreator(parent.create(position))
        }
    }

    fun header(attributes: Map<String, Any> = attr, position : Int? = null) = (element("header", attributes))

    fun footer(attributes: Map<String, Any> = attr) = element("footer", attributes)

    fun nav(attributes: Map<String, Any> = attr) = element("nav", attributes)
    }

}


open class ULElement(parent: Element) : Element(parent)
fun ElementCreator<ULElement>.li(attributes: Map<String, Any> = attr, position : Int? = null) = LIElement(parent.create().element("item", attributes))

open class LIElement(parent : Element) : Element(parent)

open class SelectCreator(val wrapped : ElementCreator) : ElementCreator(wrapped) {
    fun option(value : String, attributes: Map<String, Any> = attr)
            = element("option").setAttribute("value", value.toJson())

    fun optgroup(label : String, attributes: Map<String, Any> = attr)
            = OptGroup(element("optgroup").setAttribute("label", label.toJson()))

    fun getValue(): CompletableFuture<String>? = wrapped.evaluate("${wrapped.jsExpression}.value", {it})
    fun  setValue(value: String) = wrapped.execute("${wrapped.jsExpression}.value = ${value.toJson()};")
}
open class FormElement(wrapped: ElementCreator) : Element(wrapped)
open class OptGroup(wrapped: ElementCreator) : Element(wrapped) {
    fun option(value : String, attributes: Map<String, Any> = attr)
            = create().element("option").setAttribute("value", value.toJson())
}
open class NavElement(element: ElementCreator) : ElementCreator(element)









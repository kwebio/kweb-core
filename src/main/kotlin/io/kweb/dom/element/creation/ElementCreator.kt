package io.kweb.dom.element.creation

import io.kweb.dom.attributes.attr
import io.kweb.dom.element.Element
import io.kweb.plugins.KWebPlugin
import io.kweb.random
import io.kweb.toJson
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap
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
            appendln("var newEl = document.createElement(\"$tag\");")
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
        val newElement = Element(parent.webBrowser, tag = tag, jsExpression = "document.getElementById(\"$id\")", id = id)
        newChildListeners.values.forEach({it(newElement)})
        return newElement
    }

    fun require(vararg plugins: KClass<out KWebPlugin>) = parent.webBrowser.require(*plugins)

    private val newChildListeners = ConcurrentHashMap<Long, (Element) -> Unit>()
    fun addNewChildListener(listener : (Element) -> Unit) : Long {
        val handle = random.nextLong()
        newChildListeners[handle] = listener
        return handle
    }
    fun removeNewChildListener(handle : Long) {
        newChildListeners.remove(handle)
    }
}
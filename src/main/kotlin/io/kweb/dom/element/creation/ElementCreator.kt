package io.kweb.dom.element.creation

import io.kweb.dom.attributes.attr
import io.kweb.dom.element.Element
import io.kweb.dom.element.modification.delete
import io.kweb.plugins.KWebPlugin
import io.kweb.random
import io.kweb.toJson
import mu.KLogging
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by ian on 1/13/17.
 */

typealias Cleaner = () -> Unit

open class ElementCreator<out PARENT_TYPE : Element>(val parent: PARENT_TYPE, val position : Int? = null) {

    companion object: KLogging()

    private val cleanupListeners = LinkedList<(Cleaner) -> Unit>()

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
        val newElement = Element(parent.webBrowser, this, tag = tag, jsExpression = "document.getElementById(\"$id\")", id = id)
        onCleanup(withParent = false) {
            newElement.delete()
        }
        return newElement
    }

    fun require(vararg plugins: KClass<out KWebPlugin>) = parent.webBrowser.require(*plugins)

    fun onCleanup(withParent : Boolean, f : Cleaner) {
        if (withParent) {
            parent.creator?.onCleanup(true, f)
        }
        cleanupListeners.firstOrNull()?.invoke(f)
    }

    fun <R> withCleanupListener(listener : (Cleaner) -> Unit, receiver: ElementCreator<PARENT_TYPE>.() -> R) : R {
        this.pushCleanupListener(listener)
        val r = receiver.invoke(this)
        this.popCleanupListener()
        return r
    }

    private fun pushCleanupListener(listener : (Cleaner) -> Unit) {
        cleanupListeners.addFirst(listener)
    }

    private fun popCleanupListener() {
        cleanupListeners.removeFirst()
    }
}
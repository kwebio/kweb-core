package io.kweb.dom.element.creation

import io.kweb.*
import io.kweb.Server2ClientMessage.Instruction
import io.kweb.Server2ClientMessage.Instruction.Type.CreateElement
import io.kweb.dom.attributes.attr
import io.kweb.dom.element.*
import io.kweb.plugins.KwebPlugin
import mu.KLogging
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass

/**
 * Created by ian on 1/13/17.
 */

typealias Cleaner = () -> Unit

@KWebDSL
open class ElementCreator<out PARENT_TYPE : Element>(
        val parent: PARENT_TYPE,
        val parentCreator: ElementCreator<*>? = parent.creator,
        val position: Int? = null) {

    companion object : KLogging()

    //private val cleanupListeners = LinkedList<(Cleaner) -> Unit>()
    private val cleanupListeners = LinkedList<Cleaner>()
    private @Volatile
    var isCleanedUp = false

    val elementsCreatedCount: Int get() = elementsCreated.size

    internal
    val elementsCreated = ConcurrentLinkedQueue<Element>()

    val browser: WebBrowser get() = parent.browser

    fun element(tag: String, attributes: Map<String, Any> = attr): Element {

        if (position != null && elementsCreatedCount == 2) {
            logger.warn {
                """
                It's unwise to create multiple elements using the same ElementCreator when position is specified,
                because each element will be added at the same position among its siblings, which will result in them
                being inserted in reverse-order.
                """.trimIndent().trim()
            }
        }

        val id: String = (attributes["id"] ?: "K"+browser.generateId()).toString()
        if (parent.canSendInstruction()) {
            browser.send(Instruction(CreateElement, listOf(tag, attributes, id, parent.id, position ?: -1)))
        } else {
            parent.execute(renderJavaScriptToCreateNewElement(tag, attributes, id))
        }
        val newElement = Element(parent.browser, this, tag = tag, jsExpression = "document.getElementById(\"$id\")", id = id)
        elementsCreated += newElement
        for (plugin in parent.browser.kweb.plugins) {
            plugin.elementCreationHook(newElement)
        }
        onCleanup(withParent = false) {
            logger.debug("Deleting element ${newElement.id}", RuntimeException())
            newElement.delete()
        }
        return newElement
    }

    private fun renderJavaScriptToCreateNewElement(tag: String, attributes: Map<String, Any>, id: String): String {
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
        val js = javaScript.toString()
        return js
    }

    fun require(vararg plugins: KClass<out KwebPlugin>) = parent.browser.require(*plugins)

    /**
     * Specify a listener to be called when this element is removed from the DOM.
     *
     * @param withParent If `true` this cleaner will be called if this element is cleaned up, or if
     *                   any ancestor element of this element is cleaned up.  Otherwise it will
     *                   only be cleaned up if this element is cleaned up specifically.
     */
    fun onCleanup(withParent: Boolean, f: Cleaner) {
        if (withParent) {
            parentCreator?.onCleanup(true, f)
        }
        cleanupListeners += f
    }

    fun cleanup() {
        // TODO: Warn if called twice?
        if (!isCleanedUp) {
            isCleanedUp = true
            cleanupListeners.forEach { it() }
        }
    }
}
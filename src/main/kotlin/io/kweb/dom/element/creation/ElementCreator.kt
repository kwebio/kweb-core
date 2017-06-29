package io.kweb.dom.element.creation

import io.kweb.dom.attributes.attr
import io.kweb.dom.element.Element
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

open class ElementCreator<out PARENT_TYPE : Element>(val addToElement: PARENT_TYPE, val parentCreator : ElementCreator<*>? = addToElement.creator, val position : Int? = null) {

    companion object: KLogging()

    //private val cleanupListeners = LinkedList<(Cleaner) -> Unit>()
    private val cleanupListeners = LinkedList<Cleaner>()
    private @Volatile var isCleanedUp = false

    var elementsCreatedCount = 0

    fun element(tag: String, attributes: Map<String, Any> = attr): Element {
        elementsCreatedCount++
        if (position != null && elementsCreatedCount == 2) {
            logger.warn {
                """
                It's unwise to create multiple elements using the same ElementCreator when position is specified,
                because each element will be added at the same position among its siblings, which will result in them
                being inserted in reverse-order.
                """.trimIndent().trim() }
        }

        val id: String = (attributes["id"] ?: Math.abs(random.nextInt())).toString()
        addToElement.execute(renderJavaScriptToCreateNewElement(tag, attributes, id))
        val newElement = Element(addToElement.webBrowser, this, tag = tag, jsExpression = "document.getElementById(\"$id\")", id = id)
        onCleanup(withParent = false) {
            logger.info("Deleting element ${newElement.id}")
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
                appendln("${addToElement.jsExpression}.appendChild(newEl);")
            } else {
                appendln("${addToElement.jsExpression}.insertBefore(newEl, ${addToElement.jsExpression}.childNodes[$position]);")
            }
            appendln("}")
        }
        val js = javaScript.toString()
        return js
    }

    fun require(vararg plugins: KClass<out KWebPlugin>) = addToElement.webBrowser.require(*plugins)

    /**
     * Specify a listener to be called when this element is removed from the DOM.
     *
     * @param withParent If `true` this cleaner will be called if this element is deleted, or if
     *                   any ancestor element of this element is deleted.
     */
    fun onCleanup(withParent : Boolean, f : Cleaner) {
        if (withParent) {
            parentCreator?.onCleanup(true, f)
        }
        cleanupListeners += f
    }

    fun cleanup() {
        if (isCleanedUp) {
            throw RuntimeException("cleanup() called but it has already been called for this ElementCreator")
        }
        isCleanedUp = true
        cleanupListeners.forEach { it() }
    }

    val <E : Element> ElementCreator<E>.bind get() = RenderReceiver<E>(this)

    /*
     *
     *
    internal fun <R> withCleanupListener(listener : (Cleaner) -> Unit, receiver: ElementCreator<PARENT_TYPE>.() -> R) : R {
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
    */
}
package kweb

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kweb.html.BodyElement
import kweb.html.HeadElement
import kweb.plugins.KwebPlugin
import kweb.state.KVal
import kweb.util.KWebDSL
import kweb.util.json
import mu.KLogging
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

typealias Cleaner = () -> Unit

/**
 * Responsible for creating new DOM elements, and cleaning up [Cleaner]s, [KVar]s, and other
 * related objects when DOM elements are deleted.
 *
 * [ElementCreator] is typically used as a [receiver](https://stackoverflow.com/a/45875492)
 * for element creation functions like [p] or [element].
 */
@KWebDSL
open class ElementCreator<out PARENT_TYPE : Element>(
    val parent: PARENT_TYPE,
    val parentCreator: ElementCreator<*>? = parent.creator,
    val insertBefore: String? = null
) {

    companion object : KLogging()

    @Volatile
    private var cleanupListeners: MutableCollection<Cleaner>? = null

    @Volatile
    private var isCleanedUp = false

    val elementsCreatedCount: Int get() = elementsCreatedCountAtomic.get()
    private val elementsCreatedCountAtomic = AtomicInteger(0)

    val browser: WebBrowser get() = parent.browser

    /**
     * Create a new element, specifying its [tag](https://www.javatpoint.com/html-tags) and
     * [attributes](https://www.javatpoint.com/html-attributes).
     *
     * Tag-specific functions like [p], [select], and others call this function and should
     * be used in preference to it if available.
     *
     * @param tag The HTML tag, eg. "p", "select", "a", etc
     * @param attributes The HTML element's attributes
     * @param namespace If non-null elements will be created with [Document.createElementNS()](https://developer.mozilla.org/en-US/docs/Web/API/Document/createElementNS)
     *                  with the specified namespace. If null then Kweb will use [Document.createElement](https://developer.mozilla.org/en-US/docs/Web/API/Document/createElement).
     */
    fun element(tag: String, attributes: Map<String, JsonPrimitive> = attr, namespace: String? = null): Element {

        val mutAttributes = HashMap(attributes)

        val id: String = mutAttributes.computeIfAbsent("id") { JsonPrimitive("K" + browser.generateId()) }.content
        val htmlDoc = browser.htmlDocument.get()
        val createElementStatement = when (namespace) {
            null -> "document.createElement(tag);"
            else -> "document.createElementNS(\"${namespace}\", tag);"
        }
        when {
            htmlDoc != null -> {
                val parentElement = when (parent) {
                    is HeadElement -> htmlDoc.head()
                    is BodyElement -> htmlDoc.body()
                    else -> htmlDoc.getElementById(parent.id)
                } ?: error("Can't find element with id ${parent.id}")
                val jsElement =
                    if (insertBefore != null) {
                        val ne = htmlDoc.createElement(tag)
                        htmlDoc.getElementById(insertBefore)!!.before(ne)
                        ne
                    } else {
                        parentElement.appendElement(tag)
                    }
                for ((k, v) in mutAttributes) {
                    jsElement.attr(k, v.content)
                }
            }
            parent.browser.isCatchingOutbound() != null -> {
                //language=JavaScript
                val createElementJs = """
                    console.log("Creating new element")
                    let tag = {};
                    let attributes = {};
                    let myId = {};
                    let parentId = {};
                    let insertBefore = {};
                    console.log("insertBefore = " + insertBefore)
                    let newEl = $createElementStatement
                    newEl.setAttribute("id", myId);
                    for (const key in attributes) {
                        if ( key !== "id") {
                            newEl.setAttribute(key, attributes[key]);
                        }
                    }
                    let parentElement = document.getElementById(parentId);
                    let startNode = document.getElementById(insertBefore)
                    
                    if (insertBefore !== undefined) {
                        parentElement.insertBefore(newEl, startNode)
                    } else {
                        parentElement.appendChild(newEl);
                    }
                """.trimIndent()
                browser.callJsFunction(
                    createElementJs, JsonPrimitive(tag), JsonObject(mutAttributes), id.json,
                    JsonPrimitive(parent.id), JsonPrimitive(insertBefore ?: ""), JsonPrimitive(elementsCreatedCount)
                )
            }
            else -> {
                //The way I have written this function, instead of attributes.get(), we now use attributes[].
                //language=JavaScript
                val createElementJs = """
                    console.log("Creating new element in other place")
                    let tag = {};
                    let attributes = {};
                    let myId = {};
                    let parentId = {};
                    let insertBefore = {};
                    let newEl = document.createElement(tag);
                    if (attributes["id"] === undefined) {
                        newEl.setAttribute("id", myId);
                    }
                    for (const key in attributes) {
                            newEl.setAttribute(key, attributes[key]);
                    }
                    let parentElement = document.getElementById(parentId);
                    let startNode = document.getElementById(insertBefore)
                    
                    if (insertBefore !== undefined) {
                        parentElement.insertBefore(newEl, startNode)
                    } else {
                        parentElement.appendChild(newEl);
                    }
                """.trimIndent()
                parent.callJsFunction(
                    createElementJs, tag.json, JsonObject(mutAttributes), id.json,
                    parent.id.json, JsonPrimitive(insertBefore ?: ""), JsonPrimitive(elementsCreatedCount)
                )
            }
        }
        val newElement = Element(parent.browser, this, tag = tag, id = id)
        elementsCreatedCountAtomic.incrementAndGet()
        for (plugin in parent.browser.kweb.plugins) {
            plugin.elementCreationHook(newElement)
        }
        onCleanup(withParent = false) {
            logger.debug { "Deleting element ${newElement.id}" }
            newElement.deleteIfExists()
        }
        return newElement
    }

    /**
     * Specify that a specific plugin be provided in [Kweb.plugins], throws an exception if not.
     */
    fun require(vararg plugins: KClass<out KwebPlugin>) = parent.browser.require(*plugins)

    /**
     * Specify a listener to be called when this element is removed from the DOM.
     *
     * @param withParent If `true` this cleaner will be called if this element is cleaned up, or if
     *                   any ancestor element of this ElementCreator is cleaned up.  Otherwise it will
     *                   only be cleaned up if this ElementCreator is cleaned up specifically.
     *
     *                   As a rule-of-thumb, use 'true' for anything except deleting DOM elements.
     */
    fun onCleanup(withParent: Boolean, f: Cleaner) {
        if (withParent) {
            parentCreator?.onCleanup(true, f)
        }
        if (cleanupListeners == null)
            cleanupListeners = ConcurrentLinkedQueue()

        cleanupListeners?.add(f)
    }

    fun cleanup() {
        // TODO: Warn if called twice?
        if (!isCleanedUp) {
            isCleanedUp = true
            try {
                cleanupListeners?.forEach { it() }
            } catch (e: Exception) {
                logger.warn(e) { "Error while cleaning up ElementCreator" }
            }
        }
    }

    fun text(text: String) {
        this.parent.text(text)
    }

    fun text(text: KVal<String>) {
        this.parent.text(text)
    }
}
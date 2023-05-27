package kweb

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kweb.html.BodyElement
import kweb.html.HeadElement
import kweb.plugins.KwebPlugin
import kweb.state.CloseReason
import kweb.state.KVal
import kweb.state.KVar
import kweb.util.KWebDSL
import kweb.util.json
import mu.two.KLogging
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
    val element: PARENT_TYPE,
    val parentCreator: ElementCreator<*>? = element.creator,
    val insertBefore: String? = null
)  {

    companion object :
        KLogging()

    @Volatile
    private var cleanupListeners: MutableCollection<Cleaner>? = null

    @Volatile
    private var isCleanedUp = false

    val elementsCreatedCount: Int get() = elementsCreatedCountAtomic.get()
    private val elementsCreatedCountAtomic = AtomicInteger(0)

    val browser: WebBrowser get() = element.browser

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
    fun element(tag: String, attributes: Map<String, JsonPrimitive> = attr, namespace: String? = null, new: (ElementCreator<*>.(Element) -> Unit)? = null): Element {

        val mutAttributes = HashMap(attributes)

        val id: String = mutAttributes.computeIfAbsent("id") { JsonPrimitive("K" + browser.generateId()) }.content
        val htmlDoc = browser.htmlDocument.get()
        val createElementStatement = when (namespace) {
            null -> "document.createElement(tag);"
            else -> "document.createElementNS(\"${namespace}\", tag);"
        }
        when {
            htmlDoc != null -> {
                val parentElement = when (element) {
                    is HeadElement -> htmlDoc.head()
                    is BodyElement -> htmlDoc.body()
                    else -> htmlDoc.getElementById(element.id)
                } ?: error("Can't find element with id ${element.id}")
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

            element.browser.isCatchingOutbound() != null -> {
                //language=JavaScript
                val createElementJs = """
let tag = {};
let attributes = {};
let myId = {};
let parentId = {};
let insertBefore = {};
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
                """
                browser.callJsFunction(
                    createElementJs, JsonPrimitive(tag), JsonObject(mutAttributes), id.json,
                    JsonPrimitive(element.id), JsonPrimitive(insertBefore ?: ""), JsonPrimitive(elementsCreatedCount)
                )
            }

            else -> {
                //The way I have written this function, instead of attributes.get(), we now use attributes[].
                //language=JavaScript
                val createElementJs = """
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
                """
                element.browser.callJsFunction(
                    createElementJs, tag.json, JsonObject(mutAttributes), id.json,
                    element.id.json, JsonPrimitive(insertBefore ?: ""), JsonPrimitive(elementsCreatedCount)
                )
            }
        }
        val newElement = Element(element.browser, this, tag = tag, id = id)
        elementsCreatedCountAtomic.incrementAndGet()
        for (plugin in element.browser.kweb.plugins) {
            plugin.elementCreationHook(newElement)
        }
        onCleanup(withParent = false) {
            logger.debug { "Deleting element ${newElement.id}" }
            newElement.deleteIfExists()
        }

        if (new != null) {
            newElement.new { new(newElement) }
        }

        return newElement
    }

    /**
     * Specify that a specific plugin be provided in [Kweb.plugins], throws an exception if not.
     */
    fun require(vararg plugins: KClass<out KwebPlugin>) = element.browser.require(*plugins)

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

    /**
     * Close this AutoCloseable when this ElementCreator is cleaned up.
     */
    fun closeOnCleanup(closeable: AutoCloseable) {
        onCleanup(withParent = true) {
            closeable.close()
        }
    }

    // text() Deprecated because these may create confusion about whether element properties
    // are set on the Element or the ElementCreator
    @Deprecated("Use element.text() instead", ReplaceWith("element.text(text)"))
    fun text(text: String) {
        this.element.text(text)
    }

    @Deprecated("Use element.text() instead", ReplaceWith("element.text(text)"))
    fun text(text: KVal<String>) {
        this.element.text(text)
    }

    @Deprecated("Use element {} instead (as of v0.12.8)", ReplaceWith("element(receiver)", "kweb.ElementCreator.element"))
    fun attr(receiver : (PARENT_TYPE).() -> Unit) {
        receiver(element)
    }

    @Deprecated("Use element instead (as of v0.12.8)", ReplaceWith("element", "kweb.ElementCreator.element"))
    val parent get() = element

    @Deprecated("div { element { set(\"foo\", \"bar\")} } ===> div { it.set(\"foo\", \"bar\") }",
        ReplaceWith("receiver(element)")
    )
    fun element(receiver : (PARENT_TYPE).() -> Unit) {
        receiver(element)
    }

    /**
     * Create a new [KVar], and call [KVar.close()] when this ElementCreator is cleaned up.
     */
    fun <T> kvar(initialValue: T): KVar<T> {
        val kv = KVar(initialValue)
        onCleanup(withParent = true) {
            kv.close(CloseReason("ElementCreator cleaned up"))
        }
        return kv
    }

    /**
     * Create a new [KVar], and call [KVar.close()] when this ElementCreator is cleaned up.
     */
    fun <T> kval(initialValue: T): KVal<T> {
        val kv = KVal(initialValue)
        onCleanup(withParent = true) {
            kv.close(CloseReason("ElementCreator cleaned up"))
        }
        return kv
    }

    /**
     * Creates a CoroutineScope that will be cancelled when this ElementCreator is cleaned up.
     */
    @SinceKotlin("1.1.1")
    fun elementScope(): CoroutineScope {
        val scope = CoroutineScope(Dispatchers.IO)
        onCleanup(withParent = true) {
            scope.cancel()
        }
        return scope
    }
}
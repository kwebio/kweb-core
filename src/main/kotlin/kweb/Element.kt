package kweb

import kotlinx.serialization.json.*
import kweb.WebBrowser.CatcherType.RENDER
import kweb.html.events.Event
import kweb.html.events.EventGenerator
import kweb.html.events.OnImmediateReceiver
import kweb.html.events.OnReceiver
import kweb.html.style.StyleReceiver
import kweb.plugins.KwebPlugin
import kweb.state.CloseReason
import kweb.state.KVal
import kweb.state.KVar
import kweb.util.KWebDSL
import kweb.util.json
import kweb.util.random
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.math.abs
import kotlin.reflect.KClass

/**
 * Represents a [DOM Element](https://www.w3schools.com/jsref/dom_obj_all.asp) in a
 * remote browser window.
 */

@KWebDSL
open class Element(
    override val browser: WebBrowser,
    val creator: ElementCreator<*>?,
    val tag: String? = null,
    @Volatile var id: String
) :
    EventGenerator<Element> {
    constructor(element: Element) : this(element.browser, element.creator, tag = element.tag, id = element.id)

    /*********
     ********* Utilities for plugin creators
     *********/
    /**
     * Requires that a specific plugin or plugins be loaded by listing them
     * in the `plugins` parameter of the [Kweb] constructor.
     *
     * This should be called by any function that requires a particular plugin or
     * plugins be present.
     */
    fun assertPluginLoaded(vararg plugins: KClass<out KwebPlugin>) = browser.require(*plugins)

    /**
     * Obtain the instance of a plugin by its [KClass].
     */
    // TODO: Does this prevent the use of multiple plugins of the same class?
    fun <P : KwebPlugin> plugin(plugin: KClass<P>) = browser.plugin(plugin)

    /*********
     ********* Utilities for modifying this element
     *********/

    /**
     * A utility function to set multiple attributes in a single call, in the
     * style of [mapOf]. This is a wrapper around [set].
     */
    fun setAttributes(vararg pair: Pair<String, JsonPrimitive>): Element {
        pair.forEach { (k, v) -> set(k, v) }
        return this
    }

    /**
     * Set an attribute of this element.  For example `a().setAttribute("href", "http://kweb.io")`
     * will create an `<a>` element and set it to `<a href="http://kweb.io/">`.
     */
    operator fun set(name: String, value: JsonPrimitive) = set(name, value, null)

    /**
     * Set an attribute of this element.  For example `element["href"] = "http://kweb.io"`
     * will create an `<a>` element and set it to `<a href="http://kweb.io/">`.
     *
     * @param namespace If non-null elements will be created with [Element.setAttributeNS()](https://developer.mozilla.org/en-US/docs/Web/API/Element/setAttributeNS)
     *                  with the specified namespace. If null then Kweb will use [Element.createElement](https://developer.mozilla.org/en-US/docs/Web/API/Element/setAttribute).

     */
    fun set(name: String, value: JsonPrimitive, namespace: String? = null): Element {
        val jsoupDoc = browser.htmlDocument.get()
        val setAttributeJavaScript = when (namespace) {
            null -> "document.getElementById({}).setAttribute({}, {});"
            else -> "document.getElementById({}).setAttributeNS(\"$namespace\", {}, {});"
        }
        when {
            jsoupDoc != null && (browser.isCatchingOutbound() == null || browser.isCatchingOutbound() == RENDER) -> {
                jsoupDoc.getElementById(this.id)!!.attr(name, value.content)
            }

            else -> {
                browser.callJsFunction(
                    setAttributeJavaScript,
                    id.json, name.json, value
                )
            }
        }
        if (name.equals("id", ignoreCase = true)) {
            this.id = value.toString()
        }
        return this
    }

    operator fun set(name: String, value: String) = set(name, JsonPrimitive(value))

    operator fun set(name: String, value: Boolean) = set(name, JsonPrimitive(value))

    operator fun set(name: String, value: Number) = set(name, JsonPrimitive(value))

    /**
     * Set an attribute to the value in a [KVal], if the value changes the attribute
     * value will be updated automatically.
     */
    operator fun set(name: String, value: KVal<out JsonPrimitive>): Element {
        set(name, value.value)
        val handle = value.addListener { _, newValue ->
            set(name, newValue)
        }
        this.creator?.onCleanup(true) {
            value.removeListener(handle)
        }
        return this
    }

    /**
     * Set an attribute of this element.  For example `a().setAttribute("href", "http://kweb.io")`
     * will create an `<a>` element and set it to `<a href="http://kweb.io/">`.
     *
     * @param namespace If non-null elements will be created with [Element.setAttributeNS()](https://developer.mozilla.org/en-US/docs/Web/API/Element/setAttributeNS)
     *                  with the specified namespace. If null then Kweb will use [Element.createElement](https://developer.mozilla.org/en-US/docs/Web/API/Element/setAttribute).

     */
    @Deprecated("Use set() instead", ReplaceWith("set(name, value, namespace)"))
    fun setAttribute(name: String, value: JsonPrimitive, namespace: String? = null): Element {
        return set(name, value, namespace)
    }

    @Deprecated("use set() instead", replaceWith = ReplaceWith(expression = "set(name, value)"))
    fun setAttributeRaw(name: String, value: JsonPrimitive) = set(name, value)

    @Deprecated("use set() instead", replaceWith = ReplaceWith(expression = "set(name, value)"))
    fun setAttributeRaw(name: String, value: String) = set(name, JsonPrimitive(value))

    @Deprecated("use set() instead", replaceWith = ReplaceWith(expression = "set(name, value)"))
    fun setAttributeRaw(name: String, value: Boolean) = set(name, JsonPrimitive(value))

    @Deprecated("use set() instead", replaceWith = ReplaceWith(expression = "set(name, value)"))
    fun setAttributeRaw(name: String, value: Number) = set(name, JsonPrimitive(value))

    @Deprecated("use set() instead", replaceWith = ReplaceWith(expression = "set(name, value)"))
    fun setAttribute(name: String, value: String) = set(name, JsonPrimitive(value))

    @Deprecated("use set() instead", replaceWith = ReplaceWith(expression = "set(name, value)"))
    fun setAttribute(name: String, value: Boolean) = set(name, JsonPrimitive(value))

    @Deprecated("use set() instead", replaceWith = ReplaceWith(expression = "set(name, value)"))
    fun setAttribute(name: String, value: Number) = set(name, JsonPrimitive(value))

    /**
     * Set an attribute to the value in a [KVal], if the value changes the attribute
     * value will be updated automatically.
     */
    @Deprecated("use set() instead", replaceWith = ReplaceWith(expression = "set(name, value)"))
    fun setAttribute(name: String, value: KVal<out JsonPrimitive>) = set(name, value)

    fun removeAttribute(name: String): Element {
        val jsoupDoc = browser.htmlDocument.get()
        when {
            jsoupDoc != null && (browser.isCatchingOutbound() == null || browser.isCatchingOutbound() == RENDER) -> {
                jsoupDoc.getElementById(id)!!.removeAttr(name)
            }

            else -> {
                browser.callJsFunction("document.getElementById({}).removeAttribute({})", id.json, JsonPrimitive(name))
            }

        }
        return this
    }

    /**
     * Sets the [innerHTML](https://www.w3schools.com/jsref/prop_html_innerhtml.asp) property
     * of a DOM element.
     */
    fun innerHTML(html: String): Element {
        val jsoupDoc = browser.htmlDocument.get()
        when {
            jsoupDoc != null && (browser.isCatchingOutbound() == null || browser.isCatchingOutbound() == RENDER) -> {
                val thisEl = jsoupDoc.getElementById(this.id)!!
                thisEl.html(html)
            }

            else -> {
                browser.callJsFunction("document.getElementById({}).innerHTML = {}", id.json, JsonPrimitive(html))
            }
        }
        return this
    }

    /**
     * Sets the [innerHTML](https://www.w3schools.com/jsref/prop_html_innerhtml.asp) property
     * of a DOM element. This will be updated automatically if the value of [html] changes.
     */
    fun innerHTML(html: KVal<String>): Element {
        this.innerHTML(html.value)
        val handle = html.addListener { _, new ->
            innerHTML(new)
        }
        this.creator?.onCleanup(true) {
            html.removeListener(handle)
        }
        return this
    }

    fun focus(): Element {
        browser.callJsFunction("document.getElementById({}).focus();", id.json)
        return this
    }

    fun blur(): Element {
        browser.callJsFunction("document.getElementById({}).blur();", id.json)
        return this
    }

    /**
     * A convenience function to set the [class attribute](https://www.w3schools.com/html/html_classes.asp),
     * this is a wrapper around [setAttribute].
     */
    fun classes(value: KVal<String>) = set("class", value.map { it.json })

    /**
     * A convenience function to set the [class attribute](https://www.w3schools.com/html/html_classes.asp),
     * this is a wrapper around [set].
     */
    fun classes(vararg value: String) = setClasses(*value)

    /**
     * A convenience function to set the [class attribute](https://www.w3schools.com/html/html_classes.asp),
     * this is a wrapper around [setAttribute].
     */
    fun setClasses(vararg value: String): Element {
        set("class", value.joinToString(separator = " ").json)
        return this
    }

    /**
     * A convenience function to append a class to an existing [class attribute](https://www.w3schools.com/html/html_classes.asp).
     */
    fun addClasses(vararg classes: String, onlyIf: Boolean = true): Element {
        if (onlyIf) {
            val jsoupDoc = browser.htmlDocument.get()
            when {
                jsoupDoc != null && (browser.isCatchingOutbound() == null || browser.isCatchingOutbound() == RENDER) -> {
                    val thisEl = jsoupDoc.getElementById(this.id)!!
                    classes.forEach { thisEl.addClass(it) }
                }

                else -> {
                    for (class_ in classes) {
                        if (class_.contains(' ')) {
                            error("Class names must not contain spaces")
                        }
                        //language=JavaScript
                        browser.callJsFunction(
                            """
let id = {};
let className = {};
let el = document.getElementById(id);
if (el.classList) el.classList.add(className);
else if (!hasClass(el, className)) el.className += " " + className;
                                """, id.json, JsonPrimitive(class_)
                        )
                    }
                }
            }


        }
        return this
    }

    /**
     * A convenience function to remove one or more classes from an existing
     * [class attribute](https://www.w3schools.com/html/html_classes.asp). This will
     * be ignored if [onlyIf] is false.
     */
    fun removeClasses(vararg classes: String, onlyIf: Boolean = true): Element {
        if (onlyIf) {
            for (class_ in classes) {
                if (class_.contains(' ')) {
                    error("Class names must not contain spaces")
                }
                //language=JavaScript
                browser.callJsFunction(
                    """
let id = {};
let className = {};
let el = document.getElementById(id);
if (el.classList) el.classList.remove(className);
else if (hasClass(el, className)) {
    var reg = new RegExp("(\\s|^)" + className + "(\\s|${'$'})");
    el.className = el.className.replace(reg, " ");
}
                        """, id.json, JsonPrimitive(class_)
                )
            }
        }
        return this
    }

    fun activate(): Element {
        addClasses("is-active")
        return this
    }

    fun deactivate(): Element {
        removeClasses("is-active")
        return this
    }

    fun disable(): Element {
        set("disabled", JsonPrimitive(true))
        return this
    }

    fun enable(): Element {
        removeAttribute("disabled")
        return this
    }

    fun removeChildren(): Element {
        val jsoupDoc = browser.htmlDocument.get()
        when {
            jsoupDoc != null && (browser.isCatchingOutbound() == null || browser.isCatchingOutbound() == RENDER) -> {
                val jsoupElement = jsoupDoc.getElementById(this.id)
                jsoupElement!!.children().remove()
            }

            else -> {
                //language=JavaScript
                browser.callJsFunction(
                    """
let id = {};
if (document.getElementById(id) != null) {
    let element = document.getElementById(id);
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
}
                        """, id.json
                )
            }
        }

        return this
    }

    fun removeChildrenBetweenSpans(startSpanId: String, endSpanId: String): Element {
        val jsoupDoc = browser.htmlDocument.get()
        when {
            jsoupDoc != null && (browser.isCatchingOutbound() == null || browser.isCatchingOutbound() == RENDER) -> {
                //TODO this will only run during initial page render, and is currently untested.
                jsoupDoc.getElementById(this.id).let { jsoupElement ->
                    val startSpan = jsoupElement!!.getElementById(startSpanId)
                    val endSpan = jsoupElement.getElementById(endSpanId)
                    var nextSibling = startSpan!!.nextElementSibling()
                    while (nextSibling != endSpan) {
                        nextSibling!!.remove()
                        nextSibling = startSpan.nextElementSibling()
                    }
                }
            }

            else -> {
                //language=JavaScript
                browser.callJsFunction(
                    """
let startSpan = document.getElementById({});
let endSpan = document.getElementById({});
let nextSibling = startSpan.nextSibling;
while(nextSibling !== endSpan) {
    startSpan.parentNode.removeChild(startSpan.nextSibling);
    nextSibling = startSpan.nextSibling;
}
                        """, JsonPrimitive(startSpanId), JsonPrimitive(endSpanId)
                )
            }
        }
        return this
    }

    fun removeChildAt(position: Int): Element {
        val jsoupDoc = browser.htmlDocument.get()
        when {
            jsoupDoc != null && (browser.isCatchingOutbound() == null || browser.isCatchingOutbound() == RENDER) -> {
                jsoupDoc
                    .getElementById(this.id)!!
                    .children()[position]
                    .remove()
            }

            else -> {
                browser.callJsFunction(
                    """
let element = document.getElementById({});
element.removeChild(element.children[{}]);"""
                    , id.json, position.json
                )
            }
        }
        return this
    }

    /**
     * Set the text of this element to `value`.  Eg. `h1().text("Hello World")` will create
     * a `h1` element and set its text as follows: `<h1>Hello World</h1>`.
     */
    fun text(value: String): Element {
        val jsoupDoc = browser.htmlDocument.get()
        //language=JavaScript
        val setTextJS = """document.getElementById({}).textContent = {};"""
        when {
            jsoupDoc != null && (browser.isCatchingOutbound() == null || browser.isCatchingOutbound() == RENDER) -> {
                val element = jsoupDoc.getElementById(this.id)
                element!!.text(value)
            }

            else -> {
                browser.callJsFunction(setTextJS, id.json, JsonPrimitive(value))
            }
        }
        return this
    }

    /**
     * Set the text of this element to an [KVal] value.  If the text in the KVal
     * changes the text of this element will update automatically.
     */
    fun text(text: KVal<String>): Element {
        this.text(text.value)
        val handle = text.addListener { _, new ->
            text(new)
        }
        this.creator?.onCleanup(true) {
            text.removeListener(handle)
        }
        return this
    }

    var text: KVar<String>
        get() {
            val t = KVar("")
            text(t)
            return t
        }
        set(nv) {
            text(nv)
        }

    fun addText(value: String): Element {
        val jsoupDoc = browser.htmlDocument.get()
        //language=JavaScript
        val createTextNodeJs = """
var ntn = document.createTextNode({});
document.getElementById({}).appendChild(ntn);
        """
        when {
            jsoupDoc != null && (browser.isCatchingOutbound() == null || browser.isCatchingOutbound() == RENDER) -> {
                val element = jsoupDoc.getElementById(this.id)
                element!!.appendText(value)
            }

            else -> {
                browser.callJsFunction(createTextNodeJs, JsonPrimitive(value), id.json)
            }
        }
        return this
    }

    override fun addImmediateEventCode(eventName: String, jsCode: String) {
        //language=JavaScript
        val wrappedJS =
            """return document.getElementById({}).addEventListener({}, function(event) {$jsCode});"""
        browser.callJsFunction(wrappedJS, id.json, JsonPrimitive(eventName))
    }

    override fun addEventListener(
        eventName: String,
        returnEventFields: Set<String>,
        retrieveJs: String?,
        preventDefault: Boolean,
        callback: (JsonElement) -> Unit
    ): Element {
        val callbackId = abs(random.nextInt())
        val retrievedJs = if (retrieveJs != null) ", \"retrieved\" : ($retrieveJs)" else ""
        val eventObject =
            "{" + returnEventFields.joinToString(separator = ", ") { "\"$it\" : event.$it" } + retrievedJs + "}"
        /*It'd be nice to make eventObject a parameter, but it doesn't work.
            eventObject is a map that has entries that look like { "buttons" : event.buttons }
            the event field accessed here is the event parameter from the "function(event)" in the javascript
            There is no way to reference that event object from the server, so we use eventObject, and insert properly
            formatted JavaScript directly in the code sent to the client.
        */
        val addEventJs = """
document.getElementById({}).addEventListener({}, function(event) {
    ${if (preventDefault) "event.preventDefault();" else ""}
    callbackWs({}, $eventObject);
});
return true;
        """
        //Adding event listener was causing the client to send a Client2ServerMessage with a null data field. This caused an error
        //We make the client return true to avoid that issue.
        //Then on the server we only invoke our callback on eventObjects, by checking that payload is a JsonObject.
        //TODO we may want to fix this issue. It will probably require adding a new parameter to Server2ClientMessage
        //that will tell the client to run addEventJs, without expecting a return.
        browser.callJsFunctionWithCallback(addEventJs, callbackId, callback = { payload ->
            if (payload is JsonObject) {
                callback.invoke(payload)
            }
        }, id.json, JsonPrimitive(eventName), JsonPrimitive(callbackId))
        this.creator?.onCleanup(true) {
            browser.removeCallback(callbackId)
        }
        return this
    }

    /**
     * Return a KVar that is tied to a property related to an element, which will update when an specified
     * event fires on this element. This is a convenience wrapper around [bind].
     *
     * @param accessor Function that takes an element id and returns a JavaScript expression to access that element
     * @param updateOnEvent The event to listen for that signifies this element has been updated
     * @param initialValue The initial value of the KVar
     */
    fun bind(
        accessor: (elementId: String) -> String,
        updateOnEvent: String,
        initialValue: JsonElement = JsonPrimitive("")
    ): KVar<JsonElement> {
        return bind(
            reader = { accessor(it) },
            writer = { id, value -> "${accessor(id)} = $value" },
            updateOnEvent = updateOnEvent,
            initialValue = initialValue
        )
    }

    /**
     * Return a KVar that is tied to a property related to an element, which will update when an specified
     * event fires on this element.
     *
     * @param reader Function that takes an element id and returns a JavaScript expression to read that element
     * @param writer Function that takes an element id and a new value, and returns a JavaScript expression to
     *               write that value.
     * @param updateOnEvent The event to listen for that signifies this element has been updated
     * @param initialValue The initial value of the KVar
     */
    fun bind(
        reader: (elementId: String) -> String,
        writer: (elementId: String, value: String) -> String,
        updateOnEvent: String,
        initialValue: JsonElement = JsonPrimitive("")
    ): KVar<JsonElement> {
        val kv = KVar(initialValue)
        on(retrieveJs = reader(this.id)).event<Event>(updateOnEvent) { event ->
            kv.value = event.retrieved
        }
        val kvChangeHandler = kv.addListener { _, new ->
            browser.callJsFunction(writer(this.id, "{}") + ";", new)
        }
        creator?.onCleanup(true) {
            kv.removeListener(kvChangeHandler)
            kv.close(CloseReason("Ancestor ElementCreator cleaned up"))
        }
        browser.callJsFunction(writer(this.id, "{}") + ";", initialValue)
        return kv
    }

    /**
     * Remove this element by calling [removeChild](https://developer.mozilla.org/en-US/docs/Web/API/Node/removeChild)
     * on its parent element. An error will occur in the browser if the element doesn't exist.
     */
    fun delete() {
        //language=JavaScript
        browser.callJsFunction(
            """
let element = document.getElementById({});
element.parentNode.removeChild(element);"""
            , id.json
        )
    }

    /**
     * Remove this element by calling [removeChild](https://developer.mozilla.org/en-US/docs/Web/API/Node/removeChild)
     * on its parent element if it exists.
     */
    fun deleteIfExists() {
        try {
            //language=JavaScript
            browser.callJsFunction(
                """
let id = {}
if (document.getElementById(id)) {
    let element = document.getElementById(id);
    element.parentNode.removeChild(element);
}"""
                , id.json
            )
        } catch (e: IllegalStateException) {

        }
    }

    /**
     * Determines whether this element will be [spellchecked](https://developer.mozilla.org/en-US/docs/Web/HTML/Global_attributes/spellcheck).
     */
    fun spellcheck(spellcheck: Boolean = true) = set("spellcheck", JsonPrimitive(spellcheck))

    /**
     * Some convenience functions for modifying an element's [style attribute](https://www.w3schools.com/tags/att_style.asp).
     */
    val style get() = StyleReceiver(this)

    val flags: ConcurrentSkipListSet<String> by lazy { ConcurrentSkipListSet() }

    /**
     * See [here](https://docs.kweb.io/en/latest/dom.html#listening-for-events).
     */
    val on: OnReceiver<Element> get() = OnReceiver(this, preventDefault = false)

    /**
     * See [here](https://docs.kweb.io/en/latest/dom.html#listening-for-events).
     *
     * @param retrieveJs A JavaScript expression that will be returned to the server
     * in [Event.retrieved] when an event fires in the browser.
     * @param preventDefault Whether [preventDefault()](https://developer.mozilla.org/en-US/docs/Web/API/Event/preventDefault)
     *                       will be called on the event object.
     */
    fun on(retrieveJs: String? = null, preventDefault: Boolean = false) = OnReceiver(this, retrieveJs, preventDefault)

    /**
     * See [here](https://docs.kweb.io/en/latest/dom.html#immediate-events).
     */
    val onImmediate get() = OnImmediateReceiver(this)
}

/**
 * A convenience wrapper around [new] which allows a nested DSL-style syntax
 *
 * @param position What position among the parent's children should the new element have?
 * @param receiver A code block in which any created elements will be children of this element.
 */
fun <ELEMENT_TYPE : Element, RETURN_VALUE_TYPE> ELEMENT_TYPE.new(
    insertBefore: String? = null,
    receiver: ElementCreator<ELEMENT_TYPE>.(ELEMENT_TYPE) -> RETURN_VALUE_TYPE
)
        : RETURN_VALUE_TYPE {
    return receiver(
        ElementCreator(element = this, insertBefore = insertBefore), this
    )
}


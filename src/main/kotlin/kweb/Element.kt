package kweb

import kotlinx.serialization.json.*
import kweb.html.ElementReader
import kweb.html.events.Event
import kweb.html.events.EventGenerator
import kweb.html.events.OnImmediateReceiver
import kweb.html.events.OnReceiver
import kweb.html.style.StyleReceiver
import kweb.plugins.KwebPlugin
import kweb.state.KVal
import kweb.state.KVar
import kweb.util.KWebDSL
import kweb.util.random
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.math.abs
import kotlin.reflect.KClass

@KWebDSL
open class Element(
        override val browser: WebBrowser,
        val creator: ElementCreator<*>?,
        val tag: String? = null,
        @Volatile var id: String
) :
        EventGenerator<Element> {
    constructor(element: Element) : this(element.browser, element.creator, tag = element.tag, id = element.id)

    /**
     * Execute some JavaScript in the browser.  This is the
     * foundation upon which most other DOM modification functions in this class
     * are based. `{}`s in the js String will be replaced by the `args` values
     * in the order in which they're present in the js String.
     * 
     * Note that this will cache functions in the browser to avoid unnecessary
     * re-interpretation, making this fairly efficient.
     */
    fun callJsFunction(js: String, vararg args: JsonElement) {
        browser.callJsFunction(js, *args)
    }

    /**
     * Evaluate some JavaScript in the browser and return the result via a Future.
     * This the foundation upon which most DOM-querying functions in this class
     * are based.
     */
    suspend fun <O> callJsFunctionWithResult(js: String, outputMapper: (JsonElement) -> O, vararg args: JsonElement): O? {
        val result = browser.callJsFunctionWithResult(js, *args)
        return outputMapper.invoke(result)
    }

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
    fun <P : KwebPlugin> plugin(plugin: KClass<P>) = browser.plugin(plugin)


    val children: List<Element> = ArrayList()

    /**
     * Obtain an [ElementReader] that can be used to read various properties of this element.
     */
    //TODO, since ElementReader has been deprecated, I'm not sure if we need to mark this val as deprecated or not.
    //The IDE also complains about not supplying a replaceWith argument for this annotation.
    @Deprecated("ElementReader has been deprecated")
    open val read: ElementReader get() = ElementReader(this)

    /*********
     ********* Utilities for modifying this element
     *********/

    /**
     * Set an attribute of this element.  For example `a().setAttribute("href", "http://kweb.io")`
     * will create an `<a>` element and set it to `<a href="http://kweb.io/">`.
     *
     * Will be ignored if `value` is `null`.
     */
    fun setAttributeRaw(name: String, value: JsonPrimitive): Element {
        val htmlDoc = browser.htmlDocument.get()
        when {
            browser.kweb.isCatchingOutbound() != null -> {
                callJsFunction("document.getElementById({}).setAttribute({}, {})",
                        JsonPrimitive(id), JsonPrimitive(name), value)
            }
            htmlDoc != null -> {
                htmlDoc.getElementById(this.id).attr(name, value.content)
            }
            else -> {
                callJsFunction("document.getElementById({}).setAttribute({}, {})",
                        JsonPrimitive(id), JsonPrimitive(name), value)
            }
        }
        if (name.equals("id", ignoreCase = true)) {
            this.id = value.toString()
        }
        return this
    }

    fun setAttributeRaw(name : String, value : String)
        = setAttributeRaw(name, JsonPrimitive(value))

    fun setAttributeRaw(name : String, value : Boolean)
        = setAttributeRaw(name, JsonPrimitive(value))

    fun setAttributeRaw(name : String, value : Number)
        = setAttributeRaw(name, JsonPrimitive(value))

    fun setAttribute(name: String, oValue: KVal<out JsonPrimitive>): Element {
        setAttributeRaw(name, oValue.value)
        val handle = oValue.addListener { _, newValue ->
            setAttributeRaw(name, newValue)
        }
        this.creator?.onCleanup(true) {
            oValue.removeListener(handle)
        }
        return this
    }

    fun setAttribute(name: String, oValue: KVar<String>) : Element {
        setAttributeRaw(name, JsonPrimitive(oValue.value))
        val handle = oValue.addListener { _, newValue ->
            setAttributeRaw(name, JsonPrimitive(newValue))
        }
        this.creator?.onCleanup(true) {
            oValue.removeListener(handle)
        }
        return this
    }

    fun removeAttribute(name: String): Element {
        when {
            browser.kweb.isCatchingOutbound() != null -> {
                callJsFunction("document.getElementById({}).removeAttribute", JsonPrimitive(id), JsonPrimitive(name))
            }
            else -> {
                callJsFunction("document.getElementById({}).removeAttribute", JsonPrimitive(id), JsonPrimitive(name))
            }

        }
        return this
    }

    fun innerHTML(html: String): Element {
        val htmlDoc = browser.htmlDocument.get()
        when {
            htmlDoc != null -> {
                val thisEl = htmlDoc.getElementById(this.id)
                thisEl.html(html)
            }
            else -> {
                callJsFunction("document.getElementById({}).innerHTML = {}", JsonPrimitive(id), JsonPrimitive(html))
            }
        }
        return this
    }

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
        callJsFunction("document.getElementById({}).focus();")
        return this
    }

    fun blur(): Element {
        callJsFunction("document.getElementById({}).blur();")
        return this
    }

    fun classes(vararg value: String) = setClasses(*value)

    fun setClasses(vararg value: String): Element {
        setAttributeRaw("class", JsonPrimitive(value.joinToString(separator = " ")))
        return this
    }

    fun addClasses(vararg classes: String, onlyIf: Boolean = true): Element {
        if (onlyIf) {
            for (class_ in classes) {
                if (class_.contains(' ')) {
                    error("Class names must not contain spaces")
                }
                callJsFunction("""
                    let id = {};
                    let className = {};
                    let el = document.getElementById(id);
                    if (el.classList) el.classList.add(className);
                    else if (!hasClass(el, className)) el.className += " " + className;
                """.trimIndent(), JsonPrimitive(id), JsonPrimitive(class_))
            }
        }
        return this
    }

    fun removeClasses(vararg classes: String, onlyIf: Boolean = true): Element {
        if (onlyIf) {
            for (class_ in classes) {
                if (class_.contains(' ')) {
                    error("Class names must not contain spaces")
                }
                callJsFunction("""
                    let id = {};
                    let className = {};
                    let el = document.getElementById(id);
                    if (el.classList) el.classList.remove(className);
                    else if (hasClass(el, className)) {
                        var reg = new RegExp("(\\s|^)" + className + "(\\s|${'$'})");
                        el.className = el.className.replace(reg, " ");
                    }
                """.trimIndent(), JsonPrimitive(id), JsonPrimitive(class_))
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
        setAttributeRaw("disabled", JsonPrimitive(true))
        return this
    }

    fun enable(): Element {
        removeAttribute("disabled")
        return this
    }

    fun removeChildren(): Element {
        val htmlDoc = browser.htmlDocument.get()
        when {
            htmlDoc != null -> {
                val jsoupElement = htmlDoc.getElementById(this.id)
                    jsoupElement.children().remove()
            }
            else -> {
                callJsFunction("""
                    let id = {};
                    if (document.getElementById(id) != null) {
                        let element = document.getElementById(id);
                        while (element.firstChild) {
                            element.removeChild(element.firstChild);
                        }
                    }
                """.trimIndent(), JsonPrimitive(id))
            }
        }

        return this
    }

    fun removeChildAt(position: Int): Element {
        val htmlDoc = browser.htmlDocument.get()
        when {
            htmlDoc != null -> {
                htmlDoc.getElementById(this.id).let { jsoupElement ->
                    jsoupElement.children()[position]
                }
            }
            else -> {
                callJsFunction("""
                        let element = document.getElementById({});
                        element.removeChild(element.children[{}]);
                """.trimIndent(), JsonPrimitive(id), JsonPrimitive(position))
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
        val setTextJS = """document.getElementById({}).textContent = {};""".trimIndent()
        when {
            browser.kweb.isCatchingOutbound() != null -> {
                callJsFunction(setTextJS, JsonPrimitive(id), JsonPrimitive(value))
            }
            jsoupDoc != null -> {
                val element = jsoupDoc.getElementById(this.id)
                element.text(value)
            }
            else -> {
                callJsFunction(setTextJS, JsonPrimitive(id), JsonPrimitive(value))
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
        val createTextNodeJs = """
            var ntn = document.createTextNode({});
            document.getElementById({}).appendChild(ntn);
        """.trimIndent()
        when {
            browser.kweb.isCatchingOutbound() != null -> {
                callJsFunction(createTextNodeJs, JsonPrimitive(value), JsonPrimitive(id))
            }
            jsoupDoc != null -> {
                val element = jsoupDoc.getElementById(this.id)
                element.appendText(value)
            }
            else -> {
                callJsFunction(createTextNodeJs, JsonPrimitive(value), JsonPrimitive(id))
            }
        }
        return this
    }

    override fun addImmediateEventCode(eventName: String, jsCode: String) {
        val wrappedJS = """
            return document.getElementById({}).addEventListener({}, function(event) {
                $jsCode
            });""".trimIndent()
        browser.callJsFunction(wrappedJS, JsonPrimitive(id), JsonPrimitive(eventName))
    }

    override fun addEventListener(eventName: String, returnEventFields: Set<String>, retrieveJs: String?, callback: (JsonElement) -> Unit): Element {
        val callbackId = abs(random.nextInt())
        val retrievedJs = if (retrieveJs != null) ", \"retrieved\" : ($retrieveJs)" else ""
        val eventObject = "{" + returnEventFields.joinToString(separator = ", ") { "\"$it\" : event.$it" } + retrievedJs + "}"
        /*It'd be nice to make eventObject a parameter, but it doesn't work.
            eventObject is a map that has entries that look like { "buttons" : event.buttons }
            the event field accessed here is the event parameter from the "function(event)" in the javascript
            There is no way to reference that event object from the server, so we use eventObject, and insert properly
            formatted JavaScript directly in the code sent to the client.
        */
        val addEventJs = """
            document.getElementById({}).addEventListener({}, function(event) {
                callbackWs({}, $eventObject);
            });
            return true;
        """.trimIndent()
        //Adding event listener was causing the client to send a Client2ServerMessage with a null data field. This caused an error
        //We make the client return true to avoid that issue.
        //Then on the server we only invoke our callback on eventObjects, by checking that payload is a JsonObject.
        //TODO we may want to fix this issue. It will probably require adding a new parameter to Server2ClientMessage
        //that will tell the client to run addEventJs, without expecting a return.
        browser.callJsFunctionWithCallback(addEventJs, callbackId, callback = { payload ->
            if (payload is JsonObject) {
                callback.invoke(payload)
            }
        }, JsonPrimitive(id), JsonPrimitive(eventName), JsonPrimitive(callbackId))
        this.creator?.onCleanup(true) {
            browser.removeCallback(callbackId)
        }
        return this
    }


    fun delete() {
        callJsFunction("""
            let element = document.getElementById({});
            element.parentNode.removeChild(element);
        """.trimIndent(), JsonPrimitive(id))
    }

    fun deleteIfExists() {
        callJsFunction("""
            let id = {}
            if (document.getElementById(id)) {
                let element = document.getElementById(id);
                element.parentNode.removeChild(element);
            }
        """.trimIndent(), JsonPrimitive(id))
    }

    fun spellcheck(spellcheck: Boolean = true) = setAttributeRaw("spellcheck", JsonPrimitive(spellcheck))

    val style get() = StyleReceiver(this)

    val flags = ConcurrentSkipListSet<String>()

    /**
     * See [here](https://docs.kweb.io/en/latest/dom.html#listening-for-events).
     */
    val on: OnReceiver<Element> get() = OnReceiver(this)

    /**
     * You can supply a javascript expression `retrieveJs` which will
     * be available via [Event.retrieveJs]
     */
    fun on(retrieveJs: String) = OnReceiver(this, retrieveJs)

    /**
     * See [here](https://docs.kweb.io/en/latest/dom.html#immediate-events).
     */
    val onImmediate get() = OnImmediateReceiver(this)
}

/**
 * A convenience wrapper around [new] which allows a nested DSL-style syntax
 *
 * @Param position What position among the parent's children should the new element have?
 */
fun <ELEMENT_TYPE : Element, RETURN_VALUE_TYPE> ELEMENT_TYPE.new(
    position: Int? = null,
    receiver: ElementCreator<ELEMENT_TYPE>.() -> RETURN_VALUE_TYPE
)
        : RETURN_VALUE_TYPE {
    return receiver(
        /**
         * Returns an [ElementCreator] which can be used to create new elements and add them
         * as children of the receiver element.
         *
         * @receiver This will be the parent element of any elements created with the returned
         *           [ElementCreator]
         * @Param position What position among the parent's children should the new element have?
         */
        ElementCreator(parent = this, position = position)
    )
}


package kweb

import com.github.salomonbrys.kotson.toJson
import kweb.client.Server2ClientMessage
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
import kweb.util.escapeEcma
import kweb.util.random
import kweb.util.toJson
import java.util.concurrent.CompletableFuture
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
     * are based.
     */
    fun callJsFunction(js: String, vararg args: Any?) {
        browser.callJsFunction(js, *args)
    }

    /**
     * Evaluate some JavaScript in the browser and return the result via a Future.
     * This the foundation upon which most DOM-querying functions in this class
     * are based.
     */
    suspend fun <O> callJsFunctionWithResult(js: String, outputMapper: (Any) -> O, vararg args: Any?): O? {
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
    fun setAttributeRaw(name: String, value: Any?): Element {
        if (value != null) {
            val htmlDoc = browser.htmlDocument.get()
            when {
                browser.kweb.isCatchingOutbound() -> {
                    execute("$jsExpression.setAttribute(\"${name.escapeEcma()}\", ${value.toJson()});")
                }
                htmlDoc != null -> {
                    htmlDoc.getElementById(this.id).attr(name, value.toString())
                }
                else -> {
                    execute("$jsExpression.setAttribute(\"${name.escapeEcma()}\", ${value.toJson()});")
                }
            }
            if (name.equals("id", ignoreCase = true)) {
                this.id = value.toString()
            }
        }
        return this
    }

    fun setAttribute(name: String, oValue: KVal<out Any>): Element {
        setAttributeRaw(name, oValue.value)
        val handle = oValue.addListener { _, newValue ->
            setAttributeRaw(name, newValue)
        }
        this.creator?.onCleanup(true) {
            oValue.removeListener(handle)
        }
        return this
    }

    fun removeAttribute(name: String): Element {
        when {
            browser.kweb.isCatchingOutbound() -> {
                execute("$jsExpression.removeAttribute(\"${name.escapeEcma()}\");")
            }
            else -> {
                browser.send(Server2ClientMessage.Instruction(Server2ClientMessage.Instruction.Type.RemoveAttribute, listOf(id, name)))
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
                execute("$jsExpression.innerHTML=\"${html.escapeEcma()}\";")
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
        execute("$jsExpression.focus();")
        return this
    }

    fun blur(): Element {
        execute("$jsExpression.blur();")
        return this
    }

    fun classes(vararg value: String) = setClasses(*value)

    fun setClasses(vararg value: String): Element {
        setAttributeRaw("class", value.joinToString(separator = " ").toJson())
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
                    if (el.classList) el.classList.remove(className);
                    else if (hasClass(el, className)) el.className += " " + className;
                """.trimIndent(), id, class_.toJson())
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
                """.trimIndent(), id, class_.toJson())
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
        setAttributeRaw("disabled", true)
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
                """.trimIndent(), id)
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
                """.trimIndent(), id, position)
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
        when {
            browser.kweb.isCatchingOutbound() -> {
                execute("$jsExpression.textContent=\"${value.escapeEcma()}\"")
            }
            jsoupDoc != null -> {
                val element = jsoupDoc.getElementById(this.id)
                element.text(value)
            }
            canSendMessage() -> {
                browser.callJsFunction("""document.getElementById({}).textContent = {};""", id, value)
            }
            else -> {
                callJsFunction("document.getElementById({}).textContent = {};", id, value.escapeEcma())
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
        when {
            browser.kweb.isCatchingOutbound() -> {
                execute(
                    """
                {
                    var ntn=document.createTextNode("${value.escapeEcma()}");
                    $jsExpression.appendChild(ntn);
                }
        """.trimIndent()
                )
            }
            jsoupDoc != null -> {
                val element = jsoupDoc.getElementById(this.id)
                element.appendText(value)
            }
            canSendInstruction() -> {
                browser.send(Server2ClientMessage.Instruction(Server2ClientMessage.Instruction.Type.AddText, listOf(id, value)))
            }
            else -> {
                execute("""
                {
                    var ntn=document.createTextNode("${value.escapeEcma()}");
                    $jsExpression.appendChild(ntn);
                }
        """)
            }
        }
        return this
    }

    override fun addImmediateEventCode(eventName: String, jsCode: String) {
        val wrappedJS = "return document.getElementById({})" + """
            .addEventListener({}, function(event) {
                $jsCode
            });
        """.trimIndent()
        browser.callJsFunction(wrappedJS, id, eventName.toJson())
        /*browser.callJsFunctionWithResult(wrappedJS, id, eventName.toJson())*/
    }

    override fun addEventListener(eventName: String, returnEventFields: Set<String>, retrieveJs: String?, callback: (Any) -> Unit): Element {
        val callbackId = abs(random.nextInt())
        val retrievedJs = if (retrieveJs != null) ", \"retrieved\" : ($retrieveJs)" else ""
        val eventObject = "{" + returnEventFields.joinToString(separator = ", ") { "\"$it\" : event.$it" } + retrievedJs + "}"
        val js = """
            document.getElementById({}).addEventListener({}, function(event) {
                callbackWs($callbackId, $eventObject);
            });
        """.trimIndent()
        browser.callJsFunctionWithCallback(js, callbackId, callback = { payload ->
            callback.invoke(payload)

        }, id, eventName.toJson())
        this.creator?.onCleanup(true) {
            browser.removeCallback(callbackId)
        }
        return this
    }


    fun delete() {
        callJsFunction("""
            let element = document.getElementById({});
            element.parentNode.removeChild(element);
        """.trimIndent(), id)
    }

    fun deleteIfExists() {
        callJsFunction("""
            let id = {}
            if (document.getElementById(id)) {
                let element = document.getElementById(id);
                element.parentNode.removeChild(element);
            }
        """.trimIndent(), id)
    }

    fun spellcheck(spellcheck: Boolean = true) = setAttributeRaw("spellcheck", spellcheck)

    val style get() = StyleReceiver(this)

    val flags = ConcurrentSkipListSet<String>()

    fun canSendInstruction() = browser.kweb.isNotCatchingOutbound()

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


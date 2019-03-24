package io.kweb.dom.element

import com.github.salomonbrys.kotson.toJson
import io.kweb.*
import io.kweb.Server2ClientMessage.Instruction
import io.kweb.Server2ClientMessage.Instruction.Type
import io.kweb.Server2ClientMessage.Instruction.Type.*
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.modification.StyleReceiver
import io.kweb.dom.element.read.ElementReader
import io.kweb.plugins.KwebPlugin
import io.kweb.state.KVal
import java.util.*
import java.util.concurrent.*
import kotlin.reflect.KClass

@DslMarker
annotation class KWebDSL


// TODO: Explicit support for global attributes from http://www.w3schools.com/tags/ref_standardattributes.asp
// TODO: These should probably be accessed via a field like element.attr[GlobalAttributes.hidden], possibly
// TODO: using generics to ensure the correct return-type


@KWebDSL
open class Element(open val browser: WebBrowser, val creator: ElementCreator<*>?, open var jsExpression: String, val tag: String? = null, val id: String?) {
    constructor(element: Element) : this(element.browser, element.creator, jsExpression = element.jsExpression, tag = element.tag, id = element.id)
    /*********
     ********* Low level methods
     *********/

    /**
     * Execute some JavaScript in the browser.  This is the
     * foundation upon which most other DOM modification functions in this class
     * are based.
     */
    fun execute(js: String) {
        browser.execute(js)
    }

    /**
     * Evaluate some JavaScript in the browser and return the result via a Future.
     * This the foundation upon which most DOM-querying functions in this class
     * are based.
     */
    fun <O> evaluate(js: String, outputMapper: (String) -> O): CompletableFuture<O>? {
        return browser.evaluate(js).thenApply(outputMapper)
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
    fun setAttribute(name: String, value: Any?): Element {
        if (value != null) {
            if (canSendInstruction()) {
                browser.send(Instruction(type = SetAttribute, parameters = listOf(id, name, value)))
            } else {
                execute("$jsExpression.setAttribute(\"${name.escapeEcma()}\", ${value.toJson()});")
            }
            if (name == "id") {
                jsExpression = "document.getElementById(${value.toJson()})"
            }
        }
        return this
    }

    fun setAttribute(name : String, oValue : KVal<Any>) : Element {
        setAttribute(name, oValue.value)
        val handle = oValue.addListener { _, newValue ->
            setAttribute(name, newValue)
        }
        this.creator?.onCleanup(true) {
            oValue.removeListener(handle)
        }
        return this
    }

    fun removeAttribute(name: String): Element {
        if (canSendInstruction()) {
            browser.send(Instruction(Type.RemoveAttribute, listOf(id, name)))
        } else {
            execute("$jsExpression.removeAttribute(\"${name.escapeEcma()}\");")
        }
        return this
    }

    fun innerHTML(html: String): Element {
        execute("$jsExpression.innerHTML=\"${html.escapeEcma()}\";")
        return this
    }

    fun innerHTML(html: KVal<String>) : Element {
        this.innerHTML(html)
        val handle = html.addListener{ _, new ->
            innerHTML(new)
        }
        this.creator?.onCleanup(true) {
            html.removeListener(handle)
        }
        return this
    }

    fun focus() : Element {
        execute("$jsExpression.focus();")
        return this
    }

    fun blur() : Element {
        execute("$jsExpression.blur();")
        return this
    }

    fun setClasses(vararg value: String): Element {
        setAttribute("class", value.joinToString(separator = " ").toJson())
        return this
    }

    fun addClasses(vararg classes: String, onlyIf : Boolean = true): Element {
        if (onlyIf) {
            for (class_ in classes) {
                if (class_.contains(' ')) {
                    throw RuntimeException("Class names must not contain spaces")
                }
                execute("addClass($jsExpression, ${class_.toJson()});")
            }
        }
        return this
    }

    fun removeClasses(vararg classes: String, onlyIf: Boolean = true): Element {
        if (onlyIf) {
            for (class_ in classes) {
                if (class_.contains(' ')) {
                    throw RuntimeException("Class names must not contain spaces")
                }
                execute("removeClass($jsExpression, ${class_.toJson()});")
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
        setAttribute("disabled", true)
        return this
    }

    fun enable(): Element {
        removeAttribute("disabled")
        return this
    }

    fun removeChildren(): Element {
        execute("""
        while ($jsExpression.firstChild) {
            $jsExpression.removeChild($jsExpression.firstChild);
        }
     """.trimIndent())
        return this
    }

    fun removeChildAt(position : Int) : Element {
        execute("$jsExpression.removeChild($jsExpression.childNodes[$position]);".trimIndent())
        return this
    }

    /**
     * Set the text of this element to `value`.  Eg. `h1().text("Hello World")` will create
     * a `h1` element and set its text as follows: `<h1>Hello World</h1>`.
     */
    fun text(value: String): Element {
        if (canSendInstruction()) {
            browser.send(Instruction(SetText, listOf(id, value)))
        } else {
            execute("$jsExpression.textContent=\"${value.escapeEcma()}\"")
        }
        return this
    }

    /**
     * Set the text of this element to an [KVal] value.  If the text in the KVal
     * changes the text of this element will update automatically.
     */
    fun text(text: KVal<String>) : Element {
        this.text(text.value)
        val handle = text.addListener{ _, new ->
            text(new)
        }
        this.creator?.onCleanup(true) {
            text.removeListener(handle)
        }
        return this
    }

    fun addText(value: String): Element {
        if (canSendInstruction()) {
            browser.send(Instruction(AddText, listOf(id, value)))
        } else {
            execute("""
                {
                    var ntn=document.createTextNode("${value.escapeEcma()}");
                    $jsExpression.appendChild(ntn);
                }
        """)
        }
        return this
    }

    fun addImmediateEventCode(eventName : String, jsCode : String) {
        val wrappedJS = jsExpression + """
            .addEventListener(${eventName.toJson()}, function(event) {
                $jsCode
            });
        """.trimIndent()
        browser.evaluate(wrappedJS)
    }

    fun addEventListener(eventName: String, returnEventFields : Set<String> = Collections.emptySet(), callback: (String) -> Unit): Element {
        val callbackId = Math.abs(random.nextInt())
        val eventObject = "{"+returnEventFields.map {"\"$it\" : event.$it"}.joinToString(separator = ", ")+"}"
        val js = jsExpression + """
            .addEventListener(${eventName.toJson()}, function(event) {
                callbackWs($callbackId, $eventObject);
            });
        """
        browser.executeWithCallback(js, callbackId) { payload ->
            callback.invoke(payload)
        }
        this.creator?.onCleanup(true) {
            browser.removeCallback(callbackId)
        }
        return this
    }

    fun delete() {
        execute("$jsExpression.parentNode.removeChild($jsExpression);")
    }

    fun deleteIfExists() {
        execute("if ($jsExpression) $jsExpression.parentNode.removeChild($jsExpression);")
    }

    fun spellcheck(spellcheck : Boolean = true) = setAttribute("spellcheck", spellcheck)

    val style get() = StyleReceiver(this)

    val flags = ConcurrentSkipListSet<String>()

    fun canSendInstruction() = id != null && browser.kweb.isNotCatchingOutbound()



}

/**
 * Returns an [ElementCreator] which can be used to create new elements and add them
 * as children of the receiver element.
 *
 * @receiver This will be the parent element of any elements created with the returned
 *           [ElementCreator]
 * @Param position What position among the parent's children should the new element have?
 *
 * @sample new_sample_1
 */
fun <ELEMENT_TYPE : Element> ELEMENT_TYPE.new(position: Int? = null): ElementCreator<ELEMENT_TYPE> = ElementCreator(parent = this, position = position)

/**
 * A convenience wrapper around [new] which allows a nested DSL-style syntax
 *
 * @Param position What position among the parent's children should the new element have?
 *
 * @sample new_sample_2
 */
fun <ELEMENT_TYPE : Element, RETURN_VALUE_TYPE : Any> ELEMENT_TYPE.new(
        position : Int? = null,
        receiver: ElementCreator<ELEMENT_TYPE>.() -> RETURN_VALUE_TYPE)
        : RETURN_VALUE_TYPE {
    return receiver(new(position))
}


// Element Attribute modifier
private fun new_sample_1() {
    Kweb(port = 1234) {
        doc.body.new().h1().text("Hello World!")
    }
}

private fun new_sample_2() {
    Kweb(port = 1234) {
        doc.body.new {
            h1().text("Hello World!")
        }
    }
}


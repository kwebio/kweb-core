package io.kweb.dom.element

import io.kweb.Kweb
import io.kweb.WebBrowser
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.modification.setAttribute
import io.kweb.dom.element.modification.text
import io.kweb.dom.element.read.ElementReader
import io.kweb.plugins.KWebPlugin
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

@DslMarker
annotation class KWebDSL


@KWebDSL
open class Element (open val webBrowser: WebBrowser, internal val creator : ElementCreator<*>?, open var jsExpression: String, val tag : String? = null, val id: String? = null) {
    constructor(element: Element) : this(element.webBrowser, element.creator, jsExpression = element.jsExpression, tag = element.tag, id = element.id)
    /*********
     ********* Low level methods
     *********/

    /**
     * Execute some JavaScript in the browser.  This is the
     * foundation upon which most other DOM modification functions in this class
     * are based.
     */
    fun execute(js: String) {
        webBrowser.execute(js)
    }

    /**
     * Evaluate some JavaScript in the browser and return the result via a Future.
     * This the foundation upon which most DOM-querying functions in this class
     * are based.
     */
    fun <O> evaluate(js: String, outputMapper: (String) -> O): CompletableFuture<O>? {
        return webBrowser.evaluate(js).thenApply(outputMapper)
    }

    /*********
     ********* Element creation functions.
     *********
     ********* These allow creation of element types as children of the current element.
     ********* With the exception of element(), they do not begin with verbs, and
     ********* will typically be just the tag of the element like "div" or "input".
     *********/

    fun require(vararg plugins: KClass<out KWebPlugin>) = webBrowser.require(*plugins)

    fun <P : KWebPlugin> plugin(plugin: KClass<P>) = webBrowser.plugin(plugin)

    open val read: ElementReader get() = ElementReader(this)
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
fun <T : Element> T.new(position : Int? = null): ElementCreator<T> = ElementCreator(this, position)

/**
 * A convenience wrapper around [new] which allows a nested DSL-style syntax
 *
* @Param position What position among the parent's children should the new element have?
 *
 * @sample new_sample_2
 */
fun <T : Element, R : Any> T.new(position : Int? = null, receiver: ElementCreator<T>.() -> R) : R {
    val r = receiver(new(position))
    return r
}


// Element Attribute modifiers

fun Element.spellcheck(spellcheck : Boolean = true) = setAttribute("spellcheck", spellcheck)

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


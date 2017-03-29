package com.github.sanity.kweb.dom.element

import com.github.sanity.kweb.Kweb
import com.github.sanity.kweb.WebBrowser
import com.github.sanity.kweb.dom.element.creation.ElementCreator
import com.github.sanity.kweb.dom.element.creation.tags.h1
import com.github.sanity.kweb.dom.element.modification.setAttribute
import com.github.sanity.kweb.dom.element.modification.text
import com.github.sanity.kweb.plugins.KWebPlugin
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

@DslMarker
annotation class KWebDSL


@KWebDSL
open class Element (open val webBrowser: WebBrowser, open var jsExpression: String, val tag : String? = null, val id: String? = null) {
    constructor(element: Element) : this(element.webBrowser, jsExpression = element.jsExpression, tag = element.tag, id = element.id)
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
}

/**
 * Returns an [ElementCreator] which can be used to create new elements and add them
 * as children of the receiver element.
 *
 * @receiver This will be the parent element of any elements created with the returned
 *           [ElementCreator]
 *
 * @sample new_sample_1
 */
fun <T : Element> T.new(position : Int? = null) = ElementCreator(this, position)

/**
 * A convenience wrapper around [new] which allows a nested DSL-style syntax
 *
 * @sample new_sample_2
 */
fun <T : Element> T.new(receiver: ElementCreator<T>.() -> Unit) : T {
    receiver(new())
    return this
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


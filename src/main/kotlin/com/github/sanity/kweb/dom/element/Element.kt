package com.github.sanity.kweb.dom.element

import com.github.sanity.kweb.RootReceiver
import com.github.sanity.kweb.plugins.KWebPlugin
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

@DslMarker
annotation class KWebDSL

@KWebDSL
open class Element(open val receiver: RootReceiver, open var jsExpression: String, val tag : String? = null, val id: String? = null) {
    constructor(element: Element) : this(element.receiver, jsExpression = element.jsExpression, tag = element.tag, id = element.id)
    /*********
     ********* Low level methods
     *********/

    /**
     * Execute some JavaScript in the browser.  This is the
     * foundation upon which most other DOM modification functions in this class
     * are based.
     */
    fun execute(js: String) {
        receiver.execute(js)
    }

    /**
     * Evaluate some JavaScript in the browser and return the result via a Future.
     * This the foundation upon which most DOM-querying functions in this class
     * are based.
     */
    fun <O> evaluate(js: String, outputMapper: (String) -> O): CompletableFuture<O>? {
        return receiver.evaluate(js).thenApply(outputMapper)
    }

    fun require(requiredPlugins: KClass<out KWebPlugin>) = receiver.require(requiredPlugins)
}

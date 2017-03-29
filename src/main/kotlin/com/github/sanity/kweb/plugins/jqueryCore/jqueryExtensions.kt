package com.github.sanity.kweb.plugins.jqueryCore

import com.github.sanity.kweb.WebBrowser
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.KWebDSL
import com.github.sanity.kweb.toJson

/**
 * Created by ian on 2/22/17.
 */


// Support for $(...), since Kotlin doesn't allow methods called '$' (which is probably a good thing)
// I just use jquery()
fun Element.jquery(selector: String = "#${this.id}"): JQueryReceiver {
    require(JQueryCorePlugin::class)
    return JQueryReceiver(this.webBrowser, "$(${selector.toJson()})")
}

// And here we can implement all of the useful JQuery functions
@KWebDSL
class JQueryReceiver(internal val webBrowser: WebBrowser, internal val selectorExpression: String) {
    val on : JQueryOnReceiver get() = JQueryOnReceiver(this)

    fun execute(js : String) {
        webBrowser.execute("$selectorExpression.$js;")
    }

    fun focus() {
        execute("focus()")
    }

    fun remove() {
        execute("remove()")
    }
}

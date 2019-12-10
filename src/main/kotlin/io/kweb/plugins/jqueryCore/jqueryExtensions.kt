package io.kweb.plugins.jqueryCore

import io.kweb.WebBrowser
import io.kweb.dom.element.Element
import io.kweb.dom.element.KWebDSL
import io.kweb.toJson

/**
 * Created by ian on 2/22/17.
 */


// Support for $(...), since Kotlin doesn't allow methods called '$' (which is probably a good thing)
// I just use jquery()
fun Element.jquery(selector: String = "#${this.id}"): JQueryReceiver {
    assertPluginLoaded(JQueryCorePlugin::class)
    return JQueryReceiver(this.browser, "$(${selector.toJson()})")
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

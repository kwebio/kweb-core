package kweb.plugins.jqueryCore

import kotlinx.serialization.json.JsonPrimitive
import kweb.Element
import kweb.WebBrowser
import kweb.util.KWebDSL

/**
 * Created by ian on 2/22/17.
 */


// Support for $(...), since Kotlin doesn't allow methods called '$' (which is probably a good thing)
// I just use jquery()
fun Element.jquery(selector: String = "#${this.id}"): JQueryReceiver {
    assertPluginLoaded(JQueryCorePlugin::class)
    return JQueryReceiver(this.browser, "$(${JsonPrimitive(selector)})")
}

// And here we can implement all of the useful JQuery functions
@KWebDSL
class JQueryReceiver(internal val webBrowser: WebBrowser, internal val selectorExpression: String) {
    val on: JQueryOnReceiver get() = JQueryOnReceiver(this)

    fun focus() {
        webBrowser.callJsFunction("$selectorExpression.${"focus()"};")
    }

    fun remove() {
        webBrowser.callJsFunction("$selectorExpression.${"remove()"};")
    }
}

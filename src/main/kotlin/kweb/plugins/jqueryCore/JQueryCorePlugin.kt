package kweb.plugins.jqueryCore

import kweb.Element
import kweb.plugins.KwebPlugin
import kweb.util.json
import org.jsoup.nodes.Document


/**
 * Created by ian on 1/9/17.
 */
class JQueryCorePlugin : KwebPlugin() {
    override fun decorate(doc: Document) {
        doc.head().appendElement("script")
            .attr("src", "/kweb_static/plugins/jquery/jquery-3.6.1.min.js")
            .attr("crossorigin", "anonymous")
    }
}

val jqueryCore = JQueryCorePlugin()

/**
 * Selects the element based on id, then executes the provided js
 */
fun Element.executeOnSelf(js: String) {
    browser.callJsFunction("$('#' + {})$js", id.json)
}
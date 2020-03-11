package io.kweb.plugins.jqueryCore

import io.kweb.dom.element.Element
import io.kweb.plugins.KwebPlugin
import io.kweb.plugins.staticFiles.*
import org.jsoup.nodes.Document

private const val resourceFolder = "io/kweb/plugins/jqueryCore/static"
private const val resourceRoute = "$internalStaticFilePath/jquery"

/**
 * Created by ian on 1/9/17.
 */
class JQueryCorePlugin : KwebPlugin(dependsOn = setOf(StaticFilesPlugin(ResourceFolder(resourceFolder), resourceRoute))) {
    override fun decorate(doc : Document) {
        doc.head().appendElement("script")
                .attr("src", "/$resourceRoute/jquery-3.1.1.min.js")
                .attr("crossorigin", "anonymous")
    }
}

val jqueryCore = JQueryCorePlugin()

/**
 * Selects the element based on id, then executes the provided js
 *
 * @sample table().executeOnSelf(".tablesort()")
 */
fun Element.executeOnSelf(js: String) {
    execute("$('#$id')$js")
}
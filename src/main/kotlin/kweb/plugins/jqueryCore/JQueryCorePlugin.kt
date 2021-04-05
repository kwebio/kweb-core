package kweb.plugins.jqueryCore

import kweb.Element
import kweb.plugins.KwebPlugin
import kweb.plugins.staticFiles.ResourceFolder
import kweb.plugins.staticFiles.StaticFilesPlugin
import kweb.plugins.staticFiles.internalStaticFilePath
import kweb.table
import org.jsoup.nodes.Document

private const val resourceFolder = "kweb/plugins/jqueryCore/static"
private const val resourceRoute = "$internalStaticFilePath/jquery"

/**
 * Created by ian on 1/9/17.
 */
class JQueryCorePlugin : KwebPlugin(dependsOn = setOf(StaticFilesPlugin(ResourceFolder(resourceFolder), resourceRoute))) {
    override fun decorate(doc: Document) {
        doc.head().appendElement("script")
                .attr("src", "$resourceRoute/jquery-3.4.1.min.js")
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
    callJsFunction("$('#' + {})$js", id)
}
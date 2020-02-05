package io.kweb.plugins.jqueryCore

import io.kweb.dom.element.Element
import io.kweb.plugins.KwebPlugin
import io.kweb.plugins.staticFiles.ResourceFolder
import io.kweb.plugins.staticFiles.StaticFilesPlugin
import io.kweb.plugins.staticFiles.internalStaticFilePath

private const val resourceFolder = "io/kweb/plugins/jqueryCore/static"
private const val resourceRoute = "$internalStaticFilePath/jquery"

/**
 * Created by ian on 1/9/17.
 */
class JQueryCorePlugin : KwebPlugin(dependsOn = setOf(StaticFilesPlugin(ResourceFolder(resourceFolder), resourceRoute))) {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        // Include the plugin, this is straight from the JQuery documentation
        startHead.appendln("""
        <script
                src="$resourceRoute/jquery-3.1.1.min.js"
        crossorigin="anonymous"></script>""".trimIndent()
        )
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
package io.kweb.plugins.fomanticUI

import io.kweb.plugins.KwebPlugin
import io.kweb.plugins.jqueryCore.jqueryCore
import io.kweb.plugins.staticFiles.*
import org.jsoup.nodes.Document

private const val resourceFolder = "io/kweb/plugins/fomanticUI/static"
private const val resourceRoute = "$internalStaticFilePath/fomantic"

/**
 * Created by ian on 3/30/17.
 */
class FomanticUIPlugin : KwebPlugin(dependsOn = setOf(jqueryCore,
        StaticFilesPlugin(ResourceFolder(resourceFolder), resourceRoute))
) {
    override fun decorate(doc : Document) {
        doc.head().appendElement("link")
                .attr("rel", "stylesheet")
                .attr("type", "text/css")
                .attr("href", "$resourceRoute/semantic.min.css")

        doc.head().appendElement("script")
                .attr("src", "$resourceRoute/semantic.min.js")

    }

}

val fomanticUIPlugin = FomanticUIPlugin()

val fomantic get() = FomanticUIClasses()

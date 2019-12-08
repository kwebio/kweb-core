package io.kweb.plugins.fomanticUI

import io.kweb.plugins.KwebPlugin
import io.kweb.plugins.jqueryCore.jqueryCore
import io.kweb.plugins.staticFiles.*

private const val resourceFolder = "io/kweb/plugins/fomanticUI/static"
private const val resourceRoute = "$internalStaticFilePath/fomantic"

/**
 * Created by ian on 3/30/17.
 */
class FomanticUIPlugin : KwebPlugin() {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        startHead.append("""
            <script src="https://cdn.jsdelivr.net/npm/jquery@3.3.1/dist/jquery.min.js"></script>
            <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/fomantic-ui@2.8.2/dist/semantic.min.css">
            <script src="https://cdn.jsdelivr.net/npm/fomantic-ui@2.8.2/dist/semantic.min.js"></script>
""".trimIndent())
    }

}

val fomanticUIPlugin = FomanticUIPlugin()

val fomantic get() = FomanticUIClasses()

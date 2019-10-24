package io.kweb.plugins.fomanticUI

import io.kweb.plugins.KwebPlugin
import io.kweb.plugins.jqueryCore.jqueryCore
import io.kweb.plugins.staticFiles.*

/**
 * Created by ian on 3/30/17.
 */
class FomanticUIPlugin : KwebPlugin(dependsOn = setOf(jqueryCore, internalStaticFilesPlugin)) {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        startHead.append("""
            <script src="$internalStaticFilePath/jquery.min.js"></script>
            <link rel="stylesheet" type="text/css" href="internalStaticFilePath/semantic.min.css">
            <script src="$internalStaticFilePath/semantic.min.js"></script>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
""".trimIndent())
    }

}

val fomanticUIPlugin = FomanticUIPlugin()

val fomantic get() = FomanticUIClasses()

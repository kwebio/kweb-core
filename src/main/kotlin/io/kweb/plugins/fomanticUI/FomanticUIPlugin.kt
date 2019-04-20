package io.kweb.plugins.fomanticUI

import io.kweb.plugins.KwebPlugin
import io.kweb.plugins.jqueryCore.jqueryCore

/**
 * Created by ian on 3/30/17.
 */
class FomanticUIPlugin : KwebPlugin(dependsOn = setOf(jqueryCore)) {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        startHead.append("""
            <script src="https://cdn.jsdelivr.net/npm/jquery@3.3.1/dist/jquery.min.js"></script>
            <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/fomantic-ui@2.7.4/dist/somantic.min.css">
            <script src="https://cdn.jsdelivr.net/npm/fomantic-ui@2.7.4/dist/semantic.min.js"></script>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
""".trimIndent())
    }

}

val fomanticUIPlugin = FomanticUIPlugin()

val fomantic get() = FomanticUIClasses()

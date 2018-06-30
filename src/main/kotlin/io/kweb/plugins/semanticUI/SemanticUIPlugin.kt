package io.kweb.plugins.semanticUI

import io.kweb.plugins.KWebPlugin
import io.kweb.plugins.jqueryCore.jqueryCore

/**
 * Created by ian on 3/30/17.
 */
class SemanticUIPlugin : KWebPlugin(dependsOn = setOf(jqueryCore)) {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        startHead.append("""
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/2.2.14/semantic.min.css">
            <script src="https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/2.2.14/semantic.min.js"></script>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
""".trimIndent())
    }

}

val semanticUIPlugin = SemanticUIPlugin()

val semantic get() = SemanticUIClasses()

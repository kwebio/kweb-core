package io.kweb.plugins.semanticUI

import io.kweb.plugins.KWebPlugin
import io.kweb.plugins.jqueryCore.jqueryCore

/**
 * Created by ian on 3/30/17.
 */
class SemanticUIPlugin : KWebPlugin(dependsOn = setOf(jqueryCore)) {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        startHead.append("""
<link rel="stylesheet" href="https://cdn.jsdelivr.net/semantic-ui/2.2.2/semantic.min.css">
<script src="https://cdn.jsdelivr.net/semantic-ui/2.2.10/semantic.min.js"></script>
""".trimIndent())
    }
}

val semanticUIPlugin = SemanticUIPlugin()

val semantic get() = SemanticUIClasses()

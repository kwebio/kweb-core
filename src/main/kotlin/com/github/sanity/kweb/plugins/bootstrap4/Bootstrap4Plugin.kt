package com.github.sanity.kweb.plugins.bootstrap4

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.ElementCreator
import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.plugins.jqueryCore.jqueryCore

/**
 * Created by ian on 1/10/17.
 */

class Bootstrap4Plugin : KWebPlugin(setOf(jqueryCore)) {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        // From https://v4-alpha.getbootstrap.com/getting-started/download/#source-files
        startHead.appendln("""
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css" integrity="sha384-rwoIResjU2yc3z8GV/NPeZWAv56rSmLldC3R/AZzGRnGxQQKnKkoFVhFQhNUwEyJ" crossorigin="anonymous">
<script src="https://cdnjs.cloudflare.com/ajax/libs/tether/1.4.0/js/tether.min.js" integrity="sha384-DztdAPBWPRXSA/3eYEEUWrWCy7G5KFbe8fFjk5JAIxUYHKkDx6Qin1DkWx51bBrb" crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/js/bootstrap.min.js" integrity="sha384-vBWWzlZJ8ea9aCX4pEW3rVHjgjt7zpkNpZk+02D9phzyeVkE+jo0ieGizqPLForn" crossorigin="anonymous"></script>
        """.trimIndent()
        )
    }
}

// A convenience value
val bootstrap4 = Bootstrap4Plugin()

// The bootstrap DSL
fun ElementCreator.container(tag: String = "div", fluid: Boolean = false): Element {
    require(Bootstrap4Plugin::class)
    val fluidClassModifier = if (fluid) "-fluid" else ""
    return element(tag, attributes = mapOf("class" to "container$fluidClassModifier"))
}

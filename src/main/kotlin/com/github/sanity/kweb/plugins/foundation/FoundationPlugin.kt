package com.github.sanity.kweb.plugins.foundation

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.ElementCreator
import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.plugins.jqueryCore.jqueryCore

/**
 * Created by ian on 3/24/17.
 */

val foundation = FoundationPlugin()

class FoundationPlugin : KWebPlugin(dependsOn = setOf(jqueryCore)) {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        endHead.appendln("""<script src="https://cdn.jsdelivr.net/foundation/6.2.4/foundation.min.js"></script>""")
        endHead.appendln("""<link rel="stylesheet" href="https://cdn.jsdelivr.net/foundation/6.2.4/foundation.min.css">""")
    }

    override fun executeAfterPageCreation() = "$(document).foundation();"
}

val <ET : Element> ElementCreator<ET>.foundation : ElementCreator<FoundationElement<ET>> get() {
    require(FoundationPlugin::class)
    return ElementCreator(FoundationElement<ET>(this.parent))
}

open class FoundationElement<out PARENT_TYPE : Element>(val parent : PARENT_TYPE) : Element(parent)
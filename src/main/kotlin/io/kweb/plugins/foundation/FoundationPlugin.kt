package io.kweb.plugins.foundation

import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.plugins.KwebPlugin
import io.kweb.plugins.jqueryCore.jqueryCore

/**
 * Created by ian on 3/24/17.
 */

/*

<!-- Compressed CSS -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/foundation/6.3.0/css/foundation.min.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/foundation/6.3.0/css/foundation.min.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/foundation/6.3.0/css/foundation.min.css">

<!-- Compressed JavaScript -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/foundation/6.3.0/js/foundation.min.js"></script>
 */

val foundation = FoundationPlugin()

class FoundationPlugin : KwebPlugin(dependsOn = setOf(jqueryCore)) {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        //startHead.appendln("""<link rel="stylesheet" href="https://cdn.jsdelivr.net/normalize/6.0.0/normalize.css"></link>""")
       // startHead.appendln("""<link rel="stylesheet" href="https://cdn.jsdelivr.net/foundation/6.2.4/foundation.min.css"></link>""")
        startHead.appendln("""<script src="https://cdnjs.cloudflare.com/ajax/libs/foundation/6.3.0/js/foundation.min.js"></script>""")
        startHead.appendln("""<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/foundation/6.3.0/css/foundation.min.css">""")
     //   startHead.appendln("""<script src="https://cdn.jsdelivr.net/foundation/6.2.4/foundation.min.js"></script>""")
     //   startHead.appendln("""<script src="https://cdn.jsdelivr.net/modernizr/3.3.1/modernizr.min.js"></script>""")

    }

    override fun executeAfterPageCreation() = "$(document).foundation();"
}

val <ET : Element> ElementCreator<ET>.foundation : ElementCreator<FoundationElement<ET>> get() {
    require(FoundationPlugin::class)
    return ElementCreator(FoundationElement<ET>(this.parent))
}

open class FoundationElement<out PARENT_TYPE : Element>(val parent : PARENT_TYPE) : Element(parent)
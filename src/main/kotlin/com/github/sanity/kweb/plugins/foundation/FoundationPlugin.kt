package com.github.sanity.kweb.plugins.foundation

import com.github.sanity.kweb.plugins.KWebPlugin

/**
 * Created by ian on 3/24/17.
 */

val foundation = FoundationPlugin()

class FoundationPlugin : KWebPlugin() {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        endHead.appendln("""<script src="https://cdn.jsdelivr.net/foundation/6.2.4/foundation.min.js"></script>""")
        endHead.appendln("""<link rel="stylesheet" href="https://cdn.jsdelivr.net/foundation/6.2.4/foundation.min.css">""")
    }
}
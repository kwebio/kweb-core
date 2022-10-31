package kweb.plugins.fomanticUI

import kweb.plugins.KwebPlugin
import kweb.plugins.jqueryCore.jqueryCore
import org.jsoup.nodes.Document


// NOTE: Static assets should be updated from https://github.com/fomantic/Fomantic-UI-CSS,
//       only semantic.mic.css, semantic.min.js, components/ and themes/ are required
//       from this REPO.

/**
 * Includes the Fomantic UI framework, see: https://fomantic-ui.com/
 */
class FomanticUIPlugin : KwebPlugin(dependsOn = setOf(jqueryCore)
) {
    override fun decorate(doc: Document) {
        doc.head().appendElement("link")
                .attr("rel", "stylesheet")
                .attr("type", "text/css")
                .attr("href", "/kweb_static/plugins/fomantic/semantic.min.css")

        doc.head().appendElement("script")
                .attr("src", "/kweb_static/plugins/fomantic/semantic.min.js")

    }

}

val fomanticUIPlugin get() = FomanticUIPlugin()

val fomantic get() = FomanticUIClasses()

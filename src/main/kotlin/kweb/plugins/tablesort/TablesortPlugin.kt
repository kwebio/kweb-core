package kweb.plugins.tablesort

import kweb.dom.element.creation.tags.TableElement
import kweb.plugins.KwebPlugin
import kweb.plugins.jqueryCore.*
import org.jsoup.nodes.Document

/**
 * Uses kylexfox's [tablesort plugin](https://github.com/kylefox/jquery-tablesort) to allow sortable
 * tables.
 *
 * Usage: Create your tables as normal, then call [TableElement#sort] once. That's it!
 *
 * @author Alexander Papageorgiou Koufidis
 * @see main usage example
 */
class TablesortPlugin : KwebPlugin(dependsOn = setOf(jqueryCore)) {
    override fun decorate(doc : Document) {
        doc.head().appendElement("script")
                .attr("src", "https://semantic-ui.com/javascript/library/tablesort.js")
    }
}

val tableSortPlugin = TablesortPlugin()

/**
 * Call only once the DOM is ready (you added the table elements)
 *
 * For this to work properly for non-strings, add the `data-sort-value` attribute to your TDs!
 * [About data-sort-value](https://github.com/kylefox/jquery-tablesort#how-cells-are-sorted)
 *as
 * You don't need to call again, unless you change the DOM
 */
fun TableElement.sort() =
        apply { executeOnSelf(".tablesort()") }
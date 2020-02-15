package io.kweb.plugins.tablesort

import io.kweb.dom.element.creation.tags.TableElement
import io.kweb.plugins.KwebPlugin
import io.kweb.plugins.jqueryCore.executeOnSelf
import io.kweb.plugins.jqueryCore.jqueryCore

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
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        startHead.append("""
            <script src="https://semantic-ui.com/javascript/library/tablesort.js"></script>
""".trimIndent())
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
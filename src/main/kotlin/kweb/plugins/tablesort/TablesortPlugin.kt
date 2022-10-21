package kweb.plugins.tablesort

import kweb.*
import kweb.plugins.KwebPlugin
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.plugins.jqueryCore.executeOnSelf
import kweb.plugins.jqueryCore.jqueryCore
import org.jsoup.nodes.Document

/**
 * Uses kylexfox's [tablesort plugin](https://github.com/kylefox/jquery-tablesort) to allow sortable
 * tables.
 *
 * Usage: Create your tables as normal, then call [TableElement#sort] once. That's it!
 *
 * @author Alexander Papageorgiou Koufidis
 * @see tablesortSample Usage example
 */
class TablesortPlugin : KwebPlugin(dependsOn = setOf(jqueryCore)) {
    override fun decorate(doc: Document) {
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

fun tablesortSample() {
    Kweb(port = 4200, plugins = listOf(fomanticUIPlugin, tableSortPlugin), buildPage = {
        doc.body.new {
            table(fomantic.ui.sortable.celled.striped.table.fixed).new {
                thead().new {
                    tr().new {
                        th().text("Person")
                        th().text("Age")
                        th().text("Net")
                    }
                }

                fun ElementCreator<TbodyElement>.tableRow(person: String, age: Int, net: Long) =
                        tr().new {
                            td().text(person)
                            td().text("$age")
                            //td(mapOf("data-sort-value" to net)).text("$net")
                        }

                tbody().new {
                    tableRow("Bill", 63, 95_400_000_000)
                    tableRow("Jeff", 55, 150_000_000_000)
                    tableRow("Mark", 35, 55_000_000_000)
                    tableRow("Walt (ghost)", 118, 130_000_000_000)
                }
                // returning self so we don't have to create a val
                element
            }.sort()
            div(fomantic.ui.segment).text("""
                Try sorting the age and net column.
                
                Observe how they behave differently. The net column has a correct data-sort-value, while the age uses simple string comparison
            """.trimIndent())
        }
    })
}

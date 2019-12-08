package io.kweb.plugins.tablesort

import io.kweb.Kweb
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.new
import io.kweb.plugins.fomanticUI.fomantic
import io.kweb.plugins.fomanticUI.fomanticUIPlugin

/*

fun main(args: Array<String>) {
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
                tbody().new {
                    tableRow("Bill", 63, 95_400_000_000)
                    tableRow("Jeff", 55, 150_000_000_000)
                    tableRow("Mark", 35, 55_000_000_000)
                    tableRow("Walt (ghost)", 118, 130_000_000_000)
                }
                // returning self so we don't have to create a val
                parent
            }.sort()
            div(fomantic.ui.segment).text("""
                Try sorting the age and net column.
                
                Observe how they behave differently. The net column has a correct data-sort-value, while the age uses simple string comparison
            """.trimIndent())
        }
    })
}

private fun ElementCreator<TbodyElement>.tableRow(person: String, age: Int, net: Long) =
        tr().new {
            td().text(person)
            td().text("$age")
            td(mapOf("data-sort-value" to net)).text("$net")
        }


 */
package kweb

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * Create tables
 *
 * // @sample table_example
 */
fun ElementCreator<Element>.table(attributes: Map<String, JsonPrimitive> = attr) = TableElement(element("table", attributes))
open class TableElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.thead(attributes: Map<String, JsonPrimitive> = attr) = TheadElement(element("thead", attributes))
open class TheadElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.th(attributes: Map<String, JsonPrimitive> = attr) = ThElement(element("th", attributes))
open class ThElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.tbody(attributes: Map<String, JsonPrimitive> = attr) = TbodyElement(element("tbody", attributes))
open class TbodyElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.tr(attributes: Map<String, JsonPrimitive> = attr) = TrElement(element("tr", attributes))
open class TrElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.td(attributes: Map<String, JsonPrimitive> = attr) = TdElement(element("td", attributes))
class TdElement(parent: Element) : Element(parent)

private fun table_example() {
    Kweb(port = 2314, buildPage = {
        doc.body.new {
            table().new {
                thead().new {
                    tr().new {
                        th().text("Name"); th().text("Address")
                    }
                }
                tbody().new {
                    tr().new {
                        td().text("Ian"); td().text("Austin")
                    }
                }
            }
        }
    })
}

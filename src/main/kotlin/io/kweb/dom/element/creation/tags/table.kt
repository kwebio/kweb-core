package io.kweb.dom.element.creation.tags

import io.kweb.Kweb
import io.kweb.dom.attributes.attr
import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.new

/**
 * Create tables
 *
 * @sample table_example
 */
fun ElementCreator<Element>.table(attributes: Map<String, Any> = attr) = TableElement(element("table", attributes))
open class TableElement(parent : Element) : Element(parent)

fun ElementCreator<TableElement>.thead(attributes: Map<String, Any> = attr) = TheadElement(element("thead", attributes))
open class TheadElement(parent: Element) : Element(parent)

fun ElementCreator<TheadElement>.tr(attributes: Map<String, Any> = attr) = TrHeadElement(element("tr", attributes))
open class TrHeadElement(parent : Element) : Element(parent)

fun ElementCreator<TrHeadElement>.th(attributes: Map<String, Any> = attr) = ThElement(element("th", attributes))
open class ThElement(parent: Element) : Element(parent)

fun ElementCreator<TableElement>.tbody(attributes: Map<String, Any> = attr) = TbodyElement(element("tbody", attributes))
open class TbodyElement(parent: Element)  : Element(parent)

fun ElementCreator<TbodyElement>.tr(attributes: Map<String, Any> = attr) = TrElement(element("tr", attributes))
open class TrElement(parent: Element) : Element(parent)

fun ElementCreator<TrElement>.td(attributes: Map<String, Any> = attr) = TdElement(element("td", attributes))
class TdElement(parent: Element) : Element(parent)

fun table_example() {
    Kweb(port = 2314) {
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
    }
}

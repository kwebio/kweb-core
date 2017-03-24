package com.github.sanity.kweb.dom.element.creation

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.element.Element

/**
 * Created by ian on 1/23/17.
 */


fun ElementCreator.table(attributes: Map<String, Any> = attr): TableCreator {
    return TableCreator(element("table", attributes))
}

open class TableCreator(parent: Element) : ElementCreator(parent) {
    open fun thead(attributes: Map<String, Any> = attr) = TheadCreator(element("thead", attributes))

    open fun tbody(attributes: Map<String, Any> = attr) = TbodyCreator(element("tbody", attributes))

}

open class TheadCreator(parent: Element) : ElementCreator(parent) {
    open fun tr(attributes: Map<String, Any> = attr) = TrHeadCreator(element("tr", attributes))
}

open class TrHeadCreator(parent: Element) : ElementCreator(parent) {
    open fun th(attributes: Map<String, Any> = attr) = ThCreator(element("th", attributes))
}


open class TbodyCreator(parent: Element)  : ElementCreator(parent) {
    open fun tr(attributes: Map<String, Any> = attr) = TrBodyCreator(element("tr", attributes))

}

open class TrBodyCreator(parent: Element) : ElementCreator(parent) {
    open fun td(attributes: Map<String, Any> = attr) = TdElement(element("td", attributes))
}


open class ThCreator(parent: Element) : ElementCreator(parent) {
}

open class TdElement(parent: Element) : ElementCreator(parent) {
}
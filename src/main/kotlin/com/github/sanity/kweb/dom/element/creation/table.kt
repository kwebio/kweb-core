package com.github.sanity.kweb.dom.element.creation

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.element.Element

/**
 * Created by ian on 1/23/17.
 */


fun Element.table(attributes: Map<String, Any> = attr): TableElement {
    return TableElement(createElement("table", attributes))
}

open class TableElement(wrapped: Element) : Element(wrapped) {
    open fun thead(attributes: Map<String, Any> = attr) = TheadElement(createElement("thead", attributes))

    open fun tbody(attributes: Map<String, Any> = attr) = TbodyElement(createElement("tbody", attributes))

}

open class TheadElement(wrapped: Element) : Element(wrapped) {
    open fun tr(attributes: Map<String, Any> = attr) = TrHeadElement(createElement("tr", attributes))
}

open class TrHeadElement(wrapped: Element) : Element(wrapped) {
    open fun th(attributes: Map<String, Any> = attr) = ThElement(createElement("th", attributes))
}


open class TbodyElement(wrapped: Element)  : Element(wrapped) {
    open fun tr(attributes: Map<String, Any> = attr) = TrBodyElement(createElement("tr", attributes))

}

open class TrBodyElement(wrapped: Element) : Element(wrapped) {
    open fun td(attributes: Map<String, Any> = attr) = TdElement(createElement("td", attributes))
}


open class ThElement(wrapped: Element) : Element(wrapped) {
}

open class TdElement(wrapped: Element) : Element(wrapped) {
}
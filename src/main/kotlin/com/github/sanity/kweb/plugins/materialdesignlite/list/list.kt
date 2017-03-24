package com.github.sanity.kweb.plugins.materialdesignlite.list

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.ElementCreator
import com.github.sanity.kweb.dom.element.creation.ULCreator
import com.github.sanity.kweb.dom.element.creation.insert
import com.github.sanity.kweb.plugins.materialdesignlite.MDLCreator

/**
 * Created by ian on 1/24/17.
 */

fun MDLCreator.list(attributes: Map<String, Any> = attr) = MDLUlElement(parent.insert().ul(attributes.classes("mdl-list")))

class MDLUlElement(wrapped: ULCreator) : ULCreator(wrapped.wrapped) {
    fun item(attributes: Map<String, Any> = attr, type: ListItemType = ListItemType.oneLine)
            = MDLItemElement(element("li", attributes
            .classes("mdl-list__item")
            .classes("mdl-list__item--two-line", onlyIf = type == ListItemType.twoLine)
            .classes("mdl-list__item--three-line", onlyIf = type == ListItemType.threeLine)
    ), type)
}

enum class ListItemType {
    oneLine, twoLine, threeLine
}

class MDLItemElement(wrapped: Element, val type: ListItemType) : ElementCreator(wrapped) {
    fun primaryContent(attributes: Map<String, Any> = attr) = MDLItemElement(span(attributes.classes("mdl-list__item-primary-content")), type)
    fun avatar(attributes: Map<String, Any> = attr) = MDLItemElement(i(attributes.classes("mdl-list__item-avatar")), type)
    fun icon(attributes: Map<String, Any> = attr) = MDLItemElement(i(attributes.classes("material-icons", "mdl-list__item-icon")), type)

    fun secondaryContent(attributes: Map<String, Any> = attr): MDLItemElement {
        kotlin.require(type == ListItemType.twoLine || type == ListItemType.threeLine, { "Secondary content is only permitted if item type is twoLine or threeLine" })
        return MDLItemElement(span(attributes.classes("mdl-list__item-secondary-content")), type)
    }

    fun secondaryAction(href: String = "#", attributes: Map<String, Any> = attr): MDLItemElement {
        kotlin.require(type == ListItemType.twoLine || type == ListItemType.threeLine, { "Secondary action is only permitted if item type is twoLine or threeLine" })
        return MDLItemElement(a(href, attributes.classes("mdl-list__item-secondary-action")), type)
    }

    fun body(attributes: Map<String, Any> = attr): MDLItemElement {
        kotlin.require(type == ListItemType.threeLine, { "Secondary action is only permitted if item type is threeLine" })
        return MDLItemElement(span(attributes.classes("mdl-list__item-text-body")), type)
    }

}
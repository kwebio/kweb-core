package io.kweb.plugins.foundation.navigation

import io.kweb.Kweb
import io.kweb.dom.attributes.attr
import io.kweb.dom.attributes.classes
import io.kweb.dom.attributes.set
import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.new
import io.kweb.plugins.foundation.FoundationElement
import io.kweb.plugins.foundation.foundation

/**
 * Created by ian on 3/28/17.
 */

open class FoundationTopBarElement(parent: NavElement) : NavElement(parent)
fun ElementCreator<FoundationElement<Element>>.topBar(attributes: Map<String, Any> = attr)
        = FoundationTopBarElement(nav(attributes
        .classes("top-bar")
        .set("data-topbar", true)
        .set("role", "navigation")))

open class FoundationTitleAreaElement(parent: ULElement) : ULElement(parent)
fun ElementCreator<FoundationTopBarElement>.titleArea(attributes: Map<String, Any> = attr) = FoundationTitleAreaElement(ul(attributes.classes("title-area")))

open class FoundationNameElement(parent: LIElement) : LIElement(parent)
fun ElementCreator<FoundationTitleAreaElement>.name(attributes: Map<String, Any> = attr) = FoundationNameElement(this.li(attributes.classes("name")))

open class FoundationTopBarSectionElement(parent: Element) : Element(parent)
fun ElementCreator<FoundationTopBarElement>.topBarSection(attributes: Map<String, Any> = attr)
        = FoundationTopBarSectionElement(section(attributes.classes("top-bar-section")))

open class FoundationLeftElement(parent: Element) : Element(parent)
fun ElementCreator<FoundationTopBarSectionElement>.left(attributes: Map<String, Any> = attr)
        = FoundationRightElement(ul(attributes.classes("left")))

open class FoundationRightElement(parent: Element) : Element(parent)
fun ElementCreator<FoundationTopBarSectionElement>.right(attributes: Map<String, Any> = attr)
        = FoundationRightElement(ul(attributes.classes("right")))


fun main(args: Array<String>) {
    foundation_menu_sample()
}

private fun foundation_menu_sample() {
    Kweb(port = 1234, plugins = listOf(foundation), refreshPageOnHotswap = true) {
        doc.body.new {
            foundation.topBar().new {
                titleArea().new()
                        .name().new()
                        .h1().new()
                        .a(href="#")
                        .text("My Site")
            }
        }
    }
}
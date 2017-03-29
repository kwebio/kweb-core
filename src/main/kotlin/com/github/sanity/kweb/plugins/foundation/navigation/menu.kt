package com.github.sanity.kweb.plugins.foundation.navigation

import com.github.sanity.kweb.Kweb
import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.attributes.set
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.ElementCreator
import com.github.sanity.kweb.dom.element.creation.tags.*
import com.github.sanity.kweb.dom.element.modification.text
import com.github.sanity.kweb.dom.element.new
import com.github.sanity.kweb.plugins.foundation.FoundationElement
import com.github.sanity.kweb.plugins.foundation.foundation

/**
 * Created by ian on 3/28/17.
 */

open class FoundationTopBarElement(parent: DivElement) : FoundationElement<DivElement>(parent)
fun ElementCreator<FoundationElement<Element>>.topBar(attributes: Map<String, Any> = attr) = FoundationTopBarElement(div(attributes.classes("top-bar")))

open class FoundationTopBarLeftElement(parent: DivElement) : FoundationElement<DivElement>(parent)
fun ElementCreator<FoundationElement<FoundationTopBarElement>>.topBarLeft(attributes: Map<String, Any> = attr) = FoundationTopBarLeftElement(div(attributes.classes("top-bar-left")))

open class FoundationMenuElement(parent: ULElement) : ULElement(parent)
fun ElementCreator<FoundationElement<Element>>.menu(dropdown : Boolean? = null, attributes: Map<String, Any> = attr)
        = FoundationMenuElement(ul(attributes.classes("menu").classes("dropdown", onlyIf = dropdown ?: false).set("data-drop-down", dropdown)))

fun main(args: Array<String>) {
    foundation_menu_sample()
}

private fun foundation_menu_sample() {
    Kweb(port = 1234, plugins = listOf(foundation)) {
        doc.body.new {
            foundation.menu().new {
                li().new().a(href = "#").text("Item 1")
                li().new().a(href = "#").text("Item 2")
                li().new().a(href = "#").text("Item 3")
                li().new().a(href = "#").text("Item 4")
            }
        }
    }
}
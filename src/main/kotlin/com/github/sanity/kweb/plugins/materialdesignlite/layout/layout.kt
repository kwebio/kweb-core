package com.github.sanity.kweb.plugins.materialdesignlite.layout

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.*
import com.github.sanity.kweb.plugins.materialdesignlite.MDLElement
import java.util.*

/**
 * See [here](https://getmdl.io/components/index.html#layout-section)
 */

fun MDLElement.drawerLayout(): MDLDrawerLayoutElement = MDLDrawerLayoutElement(element.div().div(attr.classes("mdl-layout", "mdl-js-layout", "mdl-layout--fixed-drawer")))

class MDLDrawerLayoutElement(val element: DIVElement) : DIVElement(element) {
    fun drawer(attributes: Map<String, Any> = HashMap()) = MDLDrawerElement(element.div(attributes.classes("mdl-layout__drawer")))

    fun content(attributes: Map<String, Any> = HashMap()) = element.main(attributes.classes("mdl-layout__content")).div(attr.classes("page-content"))
}

class MDLDrawerElement(val element: Element) : Element(element) {
    fun title(): Element = element.span(attr.classes("mdl-layout-title"))

    fun nav(): MDLNavElement = MDLNavElement(element.nav(attr.classes("mdl-navigation")))
}

class MDLNavElement(val element: Element) : Element(element) {
    fun link(href: String? = null, attributes: Map<String, Any> = Collections.emptyMap()) = element.a(href, attributes = attributes.classes("mdl-navigation__link"))
}
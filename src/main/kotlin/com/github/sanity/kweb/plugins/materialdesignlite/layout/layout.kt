package com.github.sanity.kweb.plugins.materialdesignlite.layout

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.*
import com.github.sanity.kweb.plugins.materialdesignlite.MDLReceiver

/**
 * See [here](https://getmdl.io/components/index.html#layout-section)
 */

fun MDLReceiver.layout(
        fixedDrawer: Boolean = false,
        fixedHeader: Boolean = false,
        fixedTabs: Boolean = false,
        noDrawerButton: Boolean = false,
        noDesktopDrawerButton: Boolean = false,
        outerDivAttributes: Map<String, Any> = attr,
        layoutDivAttributes: Map<String, Any> = attr): LayoutElement {
    val outerDiv = parent.div(outerDivAttributes
            .classes("mdl-layout--fixed-drawer", onlyIf = fixedDrawer)
            .classes("mdl-layout--fixed-header", onlyIf = fixedHeader)
            .classes("mdl-layout--fixed-tabs", onlyIf = fixedTabs)
    )
    return LayoutElement(outerDiv.div(
            layoutDivAttributes
                    .classes("mdl-layout", "mdl-js-layout")
                    .classes("mdl-layout--no-drawer-button", onlyIf = noDrawerButton)
                    .classes("mdl-layout--no-desktop-drawer-button", onlyIf = noDesktopDrawerButton)
    ))
}

class LayoutElement(e: Element) : Element(e) {
    fun header(
            scroll: Boolean = false,
            waterfall: WaterfallEffects = WaterfallEffects.none,
            transparent: Boolean = false,
            seamed: Boolean = false,
            attributes: Map<String, Any> = attr
    ): LayoutHeaderElement
            = LayoutHeaderElement(header(
            attributes
                    .classes("mdl-layout__header")
                    .classes("mdl-layout__header--waterfall", onlyIf = waterfall == WaterfallEffects.normal)
                    .classes("mdl-layout__header--waterfall-hide-top", onlyIf = waterfall == WaterfallEffects.hideTop)
                    .classes("mdl-layout__header--transparent", onlyIf = transparent)
                    .classes("mdl-layout__header--seamed", onlyIf = seamed)
                    .classes("mdl-layout__header--scroll", onlyIf = scroll)
    ))

    fun drawer(attributes: Map<String, Any> = attr): LayoutDrawerElement = LayoutDrawerElement(div(attributes.classes("mdl-layout__drawer")))

    fun content(attributes: Map<String, Any> = attr) = MDLReceiver(createElement("main", attributes.classes("mdl-layout__content")))
}

enum class WaterfallEffects() {
    none, normal, hideTop
}

open class AbstractSubLayout(wrapped: DivElement) : DivElement(wrapped) {
    fun title(attributes: Map<String, Any> = attr) = span(attributes.classes("mdl-layout__title"))
    fun navigation(attributes: Map<String, Any> = attr) = NavigationElement(nav(attributes.classes("mdl-navigation")))
}

class LayoutHeaderElement(wrapped: HeaderElement) : HeaderElement(wrapped) {
    fun icon(attributes: Map<String, Any> = attr) = div(attributes.classes("mdl-layout-icon"))

    fun row(attributes: Map<String, Any> = attr) = RowElement(div(attributes.classes("mdl-layout__header-row")))

    fun tabBar(manualSwitch: Boolean = false, attributes: Map<String, Any> = attr)
            = TabElement(div(attributes
            .classes("mdl-layout__tab-bar")
            .classes("mdl-layout__tab-manual-switch", onlyIf = manualSwitch)
    ))
}

class LayoutDrawerElement(wrapped: DivElement) : AbstractSubLayout(wrapped) {

}

fun Element.spacer(attributes: Map<String, Any> = attr) = div(attributes.classes("mdl-layout-spacer"))

class RowElement(wrapped: DivElement) : AbstractSubLayout(wrapped) {
}


class NavigationElement(wrapped: NavElement) : NavElement(wrapped) {
    fun navLink(href: String? = "#", attributes: Map<String, Any> = attr) = a(href, attributes.classes("mdl-navigation__link"))
}

fun Map<String, Any>.mdlLayoutLargeScreenOnly(): Map<String, Any> = classes("mdl-layout--large-screen-only")
fun Map<String, Any>.mdlLayoutSmallScreenOnly(): Map<String, Any> = classes("mdl-layout--small-screen-only")

class TabElement(wrapped: DivElement) : DivElement(wrapped) {
    fun tab(href: String, isActive: Boolean = false, attributes: Map<String, Any> = attr) = a(href, attributes.classes("mdl-layout__tab").classes("is-active", onlyIf = isActive))
}
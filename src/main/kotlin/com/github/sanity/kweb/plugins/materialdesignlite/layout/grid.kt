package com.github.sanity.kweb.plugins.materialdesignlite.layout

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.creation.DivElement
import com.github.sanity.kweb.dom.element.creation.div
import com.github.sanity.kweb.plugins.materialdesignlite.MDLElement

/**
 * Created by ian on 1/22/17.
 */

fun MDLElement.grid(
        noSpacing: Boolean = false,
        attributes: Map<String, Any> = attr
): GridElement = GridElement(div(attributes
        .classes("mdl-grid")
        .classes("mdl-grid--no-spacing", onlyIf = noSpacing)
))

class GridElement(wrapped: DivElement) : MDLElement(wrapped) {
    // TODO: Include a check to verify that total width doesn't exceed 12
    fun div(
            width: Int,
            desktopWidth: Int? = null,
            tabletWidth: Int? = null,
            phoneWidth: Int? = null,

            offsetBefore: Int? = null,
            desktopOffsetBefore: Int? = null,
            tabletOffsetBefore: Int? = null,
            phoneOffsetBefore: Int? = null,

            order: Int? = null,
            desktopOrder: Int? = null,
            tabletOrder: Int? = null,
            phoneOrder: Int? = null,

            hideDesktop: Boolean = false,
            hideTablet: Boolean = false,
            hidePhone: Boolean = false,

            stretchVertically: Boolean = true,

            align: CellVerticalAlignment? = null,

            attributes: Map<String, Any> = attr): MDLElement = MDLElement(div(attributes
                    .classes("mdl-cell", "mdl-cell--$width-col")
                    .classes(onlyIf = desktopWidth != null, classes = "mdl-cell--$desktopWidth-desktop")
                    .classes(onlyIf = tabletWidth != null, classes = "mdl-cell--$tabletWidth-tablet")
                    .classes(onlyIf = phoneWidth != null, classes = "mdl-cell--$phoneWidth-phone")

                    .classes(onlyIf = offsetBefore != null, classes = "mdl-cell--$offsetBefore-offset")
                    .classes(onlyIf = desktopOffsetBefore != null, classes = "mdl-cell--$desktopOffsetBefore-offset-desktop")
                    .classes(onlyIf = tabletOffsetBefore != null, classes = "mdl-cell--$tabletOffsetBefore-offset-tablet")
                    .classes(onlyIf = phoneOffsetBefore != null, classes = "mdl-cell--$phoneOffsetBefore-offset-phone")


                    .classes(onlyIf = order != null, classes = "mdl-cell--order-$order")
                    .classes(onlyIf = desktopOrder != null, classes = "mdl-cell--order-$desktopOrder-desktop")
                    .classes(onlyIf = tabletOrder != null, classes = "mdl-cell--order-$tabletOrder-tablet")
                    .classes(onlyIf = phoneOrder != null, classes = "mdl-cell--order-$phoneOrder-phone")

                    .classes(onlyIf = hideDesktop, classes = "mdl-cell--hide-desktop")
                    .classes(onlyIf = hideTablet, classes = "mdl-cell--hide-tablet")
                    .classes(onlyIf = hidePhone, classes = "mdl-cell--hide-phone")

                    .classes(onlyIf = align != null, classes = "mdl-cell--$align")
            ))
}

enum class CellVerticalAlignment {
    top, middle, bottom
}
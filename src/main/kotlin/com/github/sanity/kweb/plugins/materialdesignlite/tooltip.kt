package com.github.sanity.kweb.plugins.materialdesignlite

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.attributes.set
import com.github.sanity.kweb.dom.element.Element

/**
 * Created by ian on 1/22/17.
 */

fun MDLCreator.tooltip(tag: TooltipTag = TooltipTag.div,
                       large: Boolean = false, position: TooltipPosition? = null): Element {
    kotlin.require(parent.id != null) { "id must be specified to add a tooltip to an element" }
    return element(tag.name,  attr
            .classes("mdl-tooltip")
            .classes(onlyIf = large, classes = "mdl-tooltip--large")
            .classes(onlyIf = position != null, classes = "mdl-tooltip--${position!!}")
            .set("data-mdl-for", parent.id))
}

enum class TooltipPosition {
    left, right, top, bottom
}

enum class TooltipTag {
    span, div
}
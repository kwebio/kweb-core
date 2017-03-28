package com.github.sanity.kweb.plugins.materialdesignlite

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.attributes.set
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.tags.a
import com.github.sanity.kweb.dom.element.creation.tags.div
import com.github.sanity.kweb.dom.element.creation.tags.span
import com.github.sanity.kweb.dom.element.new

/**
 * Created by ian on 1/21/17.
 */

open class MDLBadgeElement(parent: Element) : Element(parent)
fun MDLReceiver.divBadge(value: String? = null, overlap: Boolean = false, noBackground : Boolean = false,
                         attributes: Map<String, Any> = attr) = MDLBadgeElement(
        parent.new().div(attributes
                .set("data-badge", value)
                .classes("mdl-badge")
                .classes("mdl-badge--overlap", onlyIf = overlap)
                .classes("mdl-badge--no-background", onlyIf = noBackground)
        ))
fun MDLReceiver.spanBadge(value: String, overlap: Boolean = false, noBackground : Boolean = false,
                         attributes: Map<String, Any> = attr) = MDLBadgeElement(
        parent.new().span(attributes
                .set("data-badge", value)
                .classes("mdl-badge")
                .classes("mdl-badge--overlap", onlyIf = overlap)
                .classes("mdl-badge--no-background", onlyIf = noBackground)
        ))
fun MDLReceiver.aBadge(value : String, href : String? = null,
                       overlap: Boolean = false,
                       noBackground : Boolean = false,
                          attributes: Map<String, Any> = attr) = MDLBadgeElement(
        parent.new().a(href, attributes
                .set("data-badge", value)
                .classes("mdl-badge")
                .classes("mdl-badge--overlap", onlyIf = overlap)
                .classes("mdl-badge--no-background", onlyIf = noBackground)
        ))
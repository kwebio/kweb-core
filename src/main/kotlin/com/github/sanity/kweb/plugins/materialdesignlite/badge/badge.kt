package com.github.sanity.kweb.plugins.materialdesignlite.badge

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.modification.addClasses
import com.github.sanity.kweb.dom.element.modification.setAttribute
import com.github.sanity.kweb.plugins.materialdesignlite.MDLElement

/**
 * Created by ian on 1/21/17.
 */

fun MDLElement.badge(descriptor: String? = null, overlap: Boolean = false): Element {
    return addClasses("mdl-badge")
            .addClasses("mdl-badge--overlap", onlyIf = overlap)
            .setAttribute("data-badge", descriptor)
}
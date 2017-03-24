package com.github.sanity.kweb.plugins.materialdesignlite.loading

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.plugins.materialdesignlite.MDLCreator

/**
 * Created by ian on 1/24/17.
 */

fun MDLCreator.spinner(isActive: Boolean = true, singleColor: Boolean = false, attributes: Map<String, Any> = attr)
        = div(attributes
        .classes("mdl-spinner", "mdl-js-spinner")
        .classes("is-active", onlyIf = isActive)
        .classes("mdl-spinner--single-color", onlyIf = singleColor)
)
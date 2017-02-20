package com.github.sanity.kweb.plugins.materialdesignlite.button

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.creation.button
import com.github.sanity.kweb.plugins.materialdesignlite.MDLReceiver

/**
 * Created by ian on 1/21/17.
 */

fun MDLReceiver.button(displayEffect: DisplayEffect? = null, rippleEffect: Boolean = false)
        = parent.button(attributes = attr
        .classes("mdl-button", "mdl-js-button")
        .classes("mdl-button--${displayEffect}", onlyIf = displayEffect != null)
        .classes("mdl-js-rippleEffect-effect", onlyIf = rippleEffect)
)

enum class DisplayEffect(val txt: String) {
    raised("raised"), fab("fab"), miniFab("mini-fab"), icon("icon")
}

// TODO: Support colored, primary, and accent effects
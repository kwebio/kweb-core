package com.github.sanity.kweb.plugins.materialdesignlite.toggle

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.KWebDSL
import com.github.sanity.kweb.dom.element.creation.*
import com.github.sanity.kweb.plugins.materialdesignlite.MDLCreator

/**
 * See https://getmdl.io/components/index.html#toggle-section/checkbox
 */
fun MDLCreator.checkbox(rippleEffect: Boolean = false, attributes: Map<String, Any> = attr)
        = CheckboxReceiver(label(attributes
        .classes("mdl-checkbox", "mdl-js-checkbox")
        .classes("mdl-js-ripple-effect", onlyIf = rippleEffect)
))

@KWebDSL
class CheckboxReceiver(val wrapped: LabelElement) {
    fun input(attributes: Map<String, Any> = attr)
            = MDLInputElement(wrapped.create().input(attributes = attributes
            .classes("mdl-checkbox__input")))

    fun label(attributes: Map<String, Any> = attr) = MDLLabelElement(wrapped.create().label(attributes
            .classes("mdl-checkbox__label")))

}

class MDLInputElement(wrapped: InputElement) : InputElement(wrapped)
class MDLLabelElement(wrapped: LabelElement) : LabelElement(wrapped)
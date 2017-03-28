package com.github.sanity.kweb.plugins.materialdesignlite.toggle

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.KWebDSL
import com.github.sanity.kweb.dom.element.creation.ElementCreator
import com.github.sanity.kweb.dom.element.creation.tags.*
import com.github.sanity.kweb.dom.element.new
import com.github.sanity.kweb.plugins.materialdesignlite.MDLReceiver

/**
 * See https://getmdl.io/components/index.html#toggle-section/checkbox
 */
fun MDLReceiver.checkbox(for_ : Element, rippleEffect: Boolean = false, attributes: Map<String, Any> = attr) : CheckboxElement
    = checkbox(for_ = for_, rippleEffect = rippleEffect, attributes = attributes)


fun MDLReceiver.checkbox(forId : String, rippleEffect: Boolean = false, attributes: Map<String, Any> = attr)
        = CheckboxElement(parent.new().label(forId, attributes
        .classes("mdl-checkbox", "mdl-js-checkbox")
        .classes("mdl-js-ripple-effect", onlyIf = rippleEffect)
))

fun ElementCreator<CheckboxElement>.input(attributes: Map<String, Any> = attr) : InputType
        = input(attributes = attributes.classes("mdl-checkbox__input"))



@KWebDSL
class CheckboxElement(val wrapped: LabelElement)  : Element(wrapped) {

    fun input(attributes: Map<String, Any> = attr) : MDLInputElement
            = input(attributes.classes("mdl-checkbox__label"))


    fun span(attributes: Map<String, Any> = attr) : MDLSpanElement
            = span(attributes.classes("mdl-checkbox__label"))
}

class MDLInputElement(wrapped: InputElement) : InputElement(wrapped)
class MDLLabelElement(wrapped: LabelElement) : LabelElement(wrapped)
class MDLSpanElement(wrapped: SpanElement) : SpanElement(wrapped)
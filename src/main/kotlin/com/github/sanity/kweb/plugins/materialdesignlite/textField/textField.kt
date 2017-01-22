package com.github.sanity.kweb.plugins.materialdesignlite.textField

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.attributes.set
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.*
import com.github.sanity.kweb.plugins.materialdesignlite.MDLElement

/**
 * Created by ian on 1/21/17.
 */


fun MDLElement.textField(floatingLabel: Boolean = false, expandable: Boolean = false, disabled: Boolean = false, isInvalid: Boolean = false): TextFieldElement =
        TextFieldElement(div(attr.classes("mdl-textfield", "mdl-js-textfield")
                .classes("mdl-textfield--floating-label", onlyIf = floatingLabel)
                .classes("mdl-textfield--expandable", onlyIf = expandable)
                .classes("is-invalid", onlyIf = isInvalid)
                .set("disabled", disabled)))


class TextFieldElement internal constructor(val e: Element) : Element(e) {
    fun input(type: InputType? = null, pattern: String? = null, attributes: MutableMap<String, Any> = attr)
            = e.input(type, attributes = attributes.classes("mdl-textfield__input").set("pattern", pattern))

    fun label(forInput: InputElement, attributes: MutableMap<String, Any> = attr)
            = e.label(attributes
            .classes("mdl-textfield__label")
            .set("for", forInput.id ?: throw RuntimeException("Input element $forInput must specify an id to be referenced by a label")))

    fun error(attributes: MutableMap<String, Any> = attr) = span(attributes.classes("mdl-textfield__error"))
}

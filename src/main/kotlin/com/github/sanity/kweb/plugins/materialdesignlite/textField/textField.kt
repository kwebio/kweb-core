package com.github.sanity.kweb.plugins.materialdesignlite.textField

import com.github.sanity.kweb.Kweb
import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.attributes.set
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.ElementCreator
import com.github.sanity.kweb.dom.element.creation.tags.*
import com.github.sanity.kweb.dom.element.modification.text
import com.github.sanity.kweb.dom.element.new
import com.github.sanity.kweb.plugins.materialdesignlite.MDLReceiver
import com.github.sanity.kweb.plugins.materialdesignlite.materialDesignLite
import com.github.sanity.kweb.plugins.materialdesignlite.mdl
import com.github.sanity.kweb.toJson

fun MDLReceiver.form(attributes: Map<String, Any> = attr) : MDLFormElement = MDLFormElement(form(attributes))
open class MDLFormElement(parent: Element) : FormElement(parent)

/**
 *  Create a new Material Design Light textfield and related elements, see
 *  [MDL Docs](https://getmdl.io/components/index.html#textfields-section)
 *
 *  @sample textfield_example
 */
fun ElementCreator<MDLFormElement>.textField(floatingLabel: Boolean = false,
                                             expandable: Boolean = false,
                                             disabled: Boolean = false,
                                             isInvalid: Boolean = false,
                                             attributes : MutableMap<String, Any> = attr)
         = MDLTextFieldElement(div(attributes.classes("mdl-textfield", "mdl-js-textfield")
                .classes("mdl-textfield--floating-label", onlyIf = floatingLabel)
                .classes("mdl-textfield--expandable", onlyIf = expandable)
                .classes("is-invalid", onlyIf = isInvalid)
                .set("disabled", disabled)))
open class MDLTextFieldElement(parent: Element) : DivElement(parent) {
    fun change(newValue : String) {
        execute("${jsExpression}.MaterialTextfield.change(${newValue.toJson()});")
    }
}


fun ElementCreator<MDLTextFieldElement>.mdlInput(type : InputType, pattern : String? = null, attributes: Map<String, Any> = attr): MDLInputElement
        = MDLInputElement(input(type = type, attributes = attributes.classes("mdl-textfield__input").set("pattern", pattern)))
open class MDLInputElement(parent: Element) : InputElement(parent)


fun ElementCreator<MDLTextFieldElement>.mdlTextArea(rows : Int? = null, attributes: Map<String, Any> = attr): TextAreaElement
        = TextAreaElement(textArea(rows, attributes.classes("mdl-textfield__input")))

open class MDLLabelElement(parent: Element) : Element(parent)
fun ElementCreator<MDLTextFieldElement>.mdlLabel(forId : String?, attributes: Map<String, Any> = attr) : MDLLabelElement
        = MDLLabelElement(label(forId = forId, attributes = attributes.classes("mdl-textfield__label")))
fun ElementCreator<MDLTextFieldElement>.mdlLabel(for_ : Element?, attributes: Map<String, Any> = attr) : MDLLabelElement
        = mdlLabel(forId = for_?.id, attributes = attributes.classes("mdl-textfield__label"))

open class MDLSpanElement(parent: SpanElement) : SpanElement(parent)
fun ElementCreator<MDLTextFieldElement>.mdlError(attributes: Map<String, Any> = attr) = MDLSpanElement(span(attributes.classes("mdl-textfield__error")))

fun textfield_example() {
    Kweb(port = 2314, plugins = listOf(materialDesignLite)) {
        doc.body.new {
            mdl.form().new {
                textField().new {
                    val input = mdlInput(pattern = "-?[0-9]*(\\.[0-9]+)?", type = InputType.text)
                    mdlLabel(for_ = input).text("Number...")
                    mdlError().text("Input is not a number!")
                }
            }
        }
    }
}

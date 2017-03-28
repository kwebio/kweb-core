package com.github.sanity.kweb.dom.element.creation.tags

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.set
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.ElementCreator

open class ULElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.ul(attributes: Map<String, Any> = attr) = ULElement(element("ul", attributes))

open class LIElement(parent : Element) : Element(parent)
fun ElementCreator<ULElement>.li(attributes: Map<String, Any> = attr) = LIElement(element("li", attributes))

open class ButtonElement(val wrapped: Element) : Element(wrapped)
enum class ButtonType {
    button, reset, submit
}
fun ElementCreator<ButtonElement>.button(type: ButtonType? = ButtonType.button, autofocus: Boolean? = null, attributes: Map<String, Any> = attr): ButtonElement {
    return ButtonElement(element("button", attributes
            .set("type", type?.name)
            .set("autofocus", autofocus)
    ))
}


open class SpanElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.span(attributes: Map<String, Any> = attr) = SpanElement(element("span", attributes))

open class DivElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.div(attributes: Map<String, Any> = attr) = DivElement(element("div", attributes))

open class FormElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.form(attributes: Map<String, Any> = attr) = FormElement(element("form", attributes))

open class AElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.a(href : String? = null, attributes: Map<String, Any> = attr) = AElement(element("a",
        attributes.set("href", href)))

open class TextAreaElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.textArea(rows : Int? = null, attributes: Map<String, Any> = attr) = TextAreaElement(element("textarea", attributes.set("rows", rows?.toString()).set("type", "text")))

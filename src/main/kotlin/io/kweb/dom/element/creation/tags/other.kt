package io.kweb.dom.element.creation.tags

import io.kweb.dom.attributes.*
import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.read.ElementReader

open class ULElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.ul(attributes: Map<String, Any> = attr) = ULElement(element("ul", attributes))

open class LIElement(parent : Element) : Element(parent)
fun ElementCreator<ULElement>.li(attributes: Map<String, Any> = attr) = LIElement(element("li", attributes))

open class ButtonElement(val wrapped: Element) : Element(wrapped)
enum class ButtonType {
    button, reset, submit
}
fun ElementCreator<Element>.button(attributes: Map<String, Any> = attr, type: ButtonType? = ButtonType.button, autofocus: Boolean? = null): ButtonElement {
    return ButtonElement(element("button", attributes
            .set("type", type?.name)
            .set("autofocus", autofocus)
    ))
}


open class SpanElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.span(attributes: Map<String, Any> = attr) = SpanElement(element("span", attributes))

open class DivElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.div(attributes: Map<String, Any> = attr) = DivElement(element("div", attributes))

open class IElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.i(attributes: Map<String, Any> = attr) = IElement(element("i", attributes))

open class FormElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.form(attributes: Map<String, Any> = attr) = FormElement(element("form", attributes))

open class AElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.a(attributes: Map<String, Any> = attr, href : String? = "#") = AElement(element("a",
        attributes.set("href", href)))

open class TextAreaElement(parent: Element) : Element(parent) {
    override val read get() = TextAreaElementReader(this)
}
open class TextAreaElementReader(element : TextAreaElement) : ElementReader(element) {
    val value get() = receiver.evaluate("($jsExpression.innerText);")
}

fun ElementCreator<Element>.textArea(rows : Int? = null, attributes: Map<String, Any> = attr) = TextAreaElement(element("textarea", attributes.set("rows", rows?.toString()).set("type", "text")))

open class SelectElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.select(attributes: Map<String, Any> = attr) = SelectElement(element("select", attributes))

open class OptionElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.option(attributes: Map<String, Any> = attr) = OptionElement(element("option", attributes))


open class H1Element(parent: Element) : Element(parent)
fun ElementCreator<Element>.h1(attributes: Map<String, Any> = attr) = H1Element(element("h1", attributes))

open class H2Element(parent: Element) : Element(parent)
fun ElementCreator<Element>.h2(attributes: Map<String, Any> = attr) = H2Element(element("h2", attributes))

open class H3Element(parent: Element) : Element(parent)
fun ElementCreator<Element>.h3(attributes: Map<String, Any> = attr) = H3Element(element("h3", attributes))

open class H4Element(parent: Element) : Element(parent)
fun ElementCreator<Element>.h4(attributes: Map<String, Any> = attr) = H4Element(element("h4", attributes))

open class H5Element(parent: Element) : Element(parent)
fun ElementCreator<Element>.h5(attributes: Map<String, Any> = attr) = H5Element(element("h5", attributes))

open class PElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.p(attributes: Map<String, Any> = attr) = PElement(element("p", attributes))

open class NavElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.nav(attributes: Map<String, Any> = attr) = NavElement(element("nav", attributes))

open class SectionElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.section(attributes: Map<String, Any> = attr) = SectionElement(element("section", attributes))

open class ImageElement(parent: Element) : Element(parent)
/**
 * @param src The image source. The source must be from an external server since Kweb doesn't handle internal routing yet
 * @param attributes Extra attributes you want to add
 */
fun ElementCreator<Element>.img(src: String? = null, attributes: Map<String, Any> = attr) =
        ImageElement(element("img", attributes.set("src", src)))


open class CanvasElement(parent : Element) : Element(parent)
fun ElementCreator<Element>.canvas(width : Int, height : Int)
    = CanvasElement(element("canvas", mapOf("width" to width, "height" to height)))

open class BrElement(parent: Element) : Element(parent)
fun ElementCreator<Element>.br(attributes: Map<String, Any> = attr) = BrElement(element("br", attributes))
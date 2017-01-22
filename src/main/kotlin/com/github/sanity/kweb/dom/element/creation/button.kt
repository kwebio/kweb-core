package com.github.sanity.kweb.dom.element.creation

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.attributes.set

/**
 * Created by ian on 1/22/17.
 */


fun Element.button(type: ButtonType? = ButtonType.button, autofocus: Boolean? = null, attributes: Map<String, Any> = attr): ButtonElement {
    return ButtonElement(createElement("button", attributes
            .set("type", type?.name)
            .set("autofocus", autofocus)
    ))
}

open class ButtonElement(val wrapped: Element) : Element(wrapped) {

}

enum class ButtonType {
    button, reset, submit
}
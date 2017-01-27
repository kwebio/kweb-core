package com.github.sanity.kweb.dom.element.events

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.KWebDSL
import com.github.sanity.kweb.dom.element.modification.addEventListener

// TODO: Should this subclass Element?
@KWebDSL
class ONReceiver(private val parent: Element) : Element(parent) {
    fun event(eventName: String, callback: Element.() -> Unit): Element {
        parent.addEventListener(eventName, callback)
        return parent
    }

    fun click(callback: Element.() -> Unit) = event("click", callback)
    fun change(callback: Element.() -> Unit) = event("change", callback)
    fun mouseover(callback: Element.() -> Unit) = event("mouseover", callback)
    fun mouseout(callback: Element.() -> Unit) = event("mouseout", callback)
    fun keydown(callback: Element.() -> Unit) = event("keydown", callback)
    fun keypress(callback: Element.() -> Unit) = event("keypress", callback)
    fun keyup(callback: Element.() -> Unit) = event("keyup", callback)
    fun load(callback: Element.() -> Unit) = event("load", callback)
    // TODO: Add the rest http://www.w3schools.com/jsref/dom_obj_event.asp

}
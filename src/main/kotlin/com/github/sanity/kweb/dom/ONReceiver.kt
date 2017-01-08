package com.github.sanity.kweb.dom

import com.github.sanity.kweb.clientConduits.CoreReceiver

class ONReceiver(private val parent: Element) : Element(parent.receiver, parent.jsExpression) {
    fun event(eventName: String, rh: CoreReceiver.() -> Unit): ONReceiver {
        parent.addEventListener(eventName, rh)
        return this
    }

    fun click(rh: CoreReceiver.() -> Unit) = event("click", rh)
    fun change(rh: CoreReceiver.() -> Unit) = event("change", rh)
    fun mouseover(rh: CoreReceiver.() -> Unit) = event("mouseover", rh)
    fun mouseout(rh: CoreReceiver.() -> Unit) = event("mouseout", rh)
    fun keydown(rh: CoreReceiver.() -> Unit) = event("keydown", rh)
    fun load(rh: CoreReceiver.() -> Unit) = event("load", rh)
    // TODO: Add the rest http://www.w3schools.com/jsref/dom_obj_event.asp

}
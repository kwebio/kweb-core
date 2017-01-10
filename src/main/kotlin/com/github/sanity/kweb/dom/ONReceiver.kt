package com.github.sanity.kweb.dom

import com.github.sanity.kweb.RootReceiver

// TODO: Should this subclass Element?
class ONReceiver(private val parent: Element) : Element(parent.receiver, parent.jsExpression) {
    fun event(eventName: String, rh: RootReceiver.() -> Unit): ONReceiver {
        parent.addEventListener(eventName, rh)
        return this
    }

    fun click(rh: RootReceiver.() -> Unit) = event("click", rh)
    fun change(rh: RootReceiver.() -> Unit) = event("change", rh)
    fun mouseover(rh: RootReceiver.() -> Unit) = event("mouseover", rh)
    fun mouseout(rh: RootReceiver.() -> Unit) = event("mouseout", rh)
    fun keydown(rh: RootReceiver.() -> Unit) = event("keydown", rh)
    fun load(rh: RootReceiver.() -> Unit) = event("load", rh)
    // TODO: Add the rest http://www.w3schools.com/jsref/dom_obj_event.asp

}
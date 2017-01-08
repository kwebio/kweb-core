package com.github.sanity.kweb.dom

import com.github.sanity.kweb.clientConduits.CoreReceiver

class ONReceiver(private val parent: HTMLReceiver) : HTMLReceiver(parent) {
    fun event(eventName: String, rh: CoreReceiver.() -> Boolean): ONReceiver {
        parent.addEventListener(eventName, rh)
        return this
    }

    fun click(rh: CoreReceiver.() -> Boolean) = event("click", rh)
    fun change(rh: CoreReceiver.() -> Boolean) = event("change", rh)
    fun mouseover(rh: CoreReceiver.() -> Boolean) = event("mouseover", rh)
    fun mouseout(rh: CoreReceiver.() -> Boolean) = event("mouseout", rh)
    fun keydown(rh: CoreReceiver.() -> Boolean) = event("keydown", rh)
    fun load(rh: CoreReceiver.() -> Boolean) = event("load", rh)
    // TODO: Add the rest http://www.w3schools.com/jsref/dom_obj_event.asp

}
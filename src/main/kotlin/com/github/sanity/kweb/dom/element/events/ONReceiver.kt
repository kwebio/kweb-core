package com.github.sanity.kweb.dom.element.events

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.KWebDSL
import com.github.sanity.kweb.dom.element.modification.addEventListener
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

// TODO: Should this subclass Element?
@KWebDSL
class ONReceiver(private val parent: Element) : Element(parent) {
    fun event(eventName: String, callback: Element.(String) -> Unit): Element {
        parent.addEventListener(eventName, callback = callback)
        return parent
    }

    fun click(callback: Element.(String) -> Unit) = event("click", callback = callback)
    fun change(callback: Element.(String) -> Unit) = event("change",callback =  callback)
    fun mouseover(callback: Element.(String) -> Unit) = event("mouseover",callback =  callback)
    fun mouseout(callback: Element.(String) -> Unit) = event("mouseout",callback =  callback)
    fun keydown(callback: Element.(String) -> Unit) = event("keydown",callback =  callback)
    fun keypress(callback: Element.(String) -> Unit) = event("keypress",callback =  callback)
    fun keyup(callback: Element.(String) -> Unit) = event("keyup",callback =  callback)
    fun load(callback: Element.(String) -> Unit) = event("load",callback =  callback)
    // TODO: Add the rest http://www.w3schools.com/jsref/dom_obj_event.asp
}

/* TODO: Get event data back from the client, using returnEventFields in Element.addEventListener
 (was having issues getting kotson to work, which blocked this)
*/
    data class KeyboardEvent(
            val code : String, val ctrlKey : Boolean, val key : String,
            val location : Int, val metaKey : Boolean, val repeat : Boolean,
            val shiftKey : Boolean, val isComposing : Boolean)

private fun getPropertySet(cls : KClass<*>) : Set<String> = cls.declaredMemberProperties.map {it.name}.toSet()


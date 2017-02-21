package com.github.sanity.kweb.dom.element.events

import com.github.salomonbrys.kotson.fromJson
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.KWebDSL
import com.github.sanity.kweb.dom.element.modification.addEventListener
import com.github.sanity.kweb.gson
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

// TODO: Should this subclass Element?
@KWebDSL
class ONReceiver(private val parent: Element) : Element(parent) {
    open class Event(open val type : String)

    open class UIEvent(
            override val type : String,
            open val detail : Long
    ) : Event(type)

    data class MouseEvent(override val type : String, override val detail : Long,
                          val altKey : Boolean, val button : Int, val buttons : Int,
                          val clientX : Int, val clientY : Int, val ctrlKey : Boolean,
                          val metaKey : Boolean, val movementX : Int, val movementY : Int,
                          val region : String, val screenX : Int, val screenY : Int,
                          val shiftKey : Boolean, val x : Int = clientX, val y : Int = clientY
                             ) : UIEvent(type, detail)

    data class KeyboardEvent(override val type : String, override val detail : Long,
                             val key : String, val altKey : Boolean,
                             val ctrlKey : Boolean, val code : String,
                             val location : Int, val metaKey : Boolean,
                             val shiftKey : Boolean, val locale : String,
                             val isComposing : Boolean) : UIEvent(type, detail)

    fun event(eventName: String, returnEventFields: Set<String> = emptySet(), callback: (String) -> Unit): Element {
        parent.addEventListener(eventName, returnEventFields = returnEventFields, callback = callback)
        return parent
    }

    inline fun <reified T : Any> event(eventName : String, eventType : KClass<T>, crossinline callback : (T)-> Unit) : Element {
        // TODO: Should probably cache this rather than do the reflection every time
        val eventPropertyNames = KeyboardEvent::class.memberProperties.map {it.name}.toSet()
        return event(eventName, eventPropertyNames, {propertiesAsString ->
            val props : T = gson.fromJson(propertiesAsString)
            callback(props)
        })
    }

    fun click(callback: (MouseEvent) -> Unit) = event("click", eventType = MouseEvent::class, callback = callback)
    fun change(callback: (MouseEvent) -> Unit) = event("change", eventType = MouseEvent::class, callback = callback)
    fun mouseover(callback: (MouseEvent) -> Unit) = event("mouseover", eventType = MouseEvent::class, callback = callback)
    fun mouseout(callback: (MouseEvent) -> Unit) = event("mouseout", eventType = MouseEvent::class, callback = callback)

    fun keydown(callback: (KeyboardEvent) -> Unit) = event("keydown", eventType = KeyboardEvent::class, callback =  callback)
    fun keypress(callback: (KeyboardEvent) -> Unit) = event("keypress", eventType = KeyboardEvent::class, callback =  callback)
    fun keyup(callback: (KeyboardEvent) -> Unit) = event("keyup", eventType = KeyboardEvent::class, callback =  callback)

    fun load(callback: (String) -> Unit) = event("load", callback = callback)

    fun focus(callback: (String) -> Unit) = event("focus", callback = callback)
    fun focusin(callback: (String) -> Unit) = event("focusin", callback = callback)
    fun focusout(callback: (String) -> Unit) = event("focusout", callback = callback)
    fun blur(callback: (String) -> Unit) = event("blur", callback = callback)
    // TODO: Add the rest http://www.w3schools.com/jsref/dom_obj_event.asp
}

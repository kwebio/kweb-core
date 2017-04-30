package io.kweb.dom.element.events

import com.github.salomonbrys.kotson.fromJson
import io.kweb.dom.element.Element
import io.kweb.dom.element.KWebDSL
import io.kweb.dom.element.modification.addEventListener
import io.kweb.gson
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

// TODO: Should this subclass Element?
@KWebDSL
open class ONReceiver(private val parent: Element) : Element(parent) {
    val logger = KotlinLogging.logger {}

    open class Event(open val type : String)

    data class MouseEvent(val type : String, val detail : Long,
                          val altKey : Boolean, val button : Int, val buttons : Int,
                          val clientX : Int, val clientY : Int, val ctrlKey : Boolean,
                          val metaKey : Boolean, val movementX : Int, val movementY : Int,
                          val region : String, val screenX : Int, val screenY : Int,
                          val shiftKey : Boolean, val x : Int = clientX, val y : Int = clientY
                             )

    data class KeyboardEvent(val type : String,  val detail : Long,
                             val key : String, val altKey : Boolean,
                             val ctrlKey : Boolean, val code : String,
                             val location : Int, val metaKey : Boolean,
                             val shiftKey : Boolean, val locale : String,
                             val isComposing : Boolean)

    data class AnimationEvent(val type : String,  val detail : Long,
                             val animationName : String, val elapsedTime : Int)

    fun event(eventName: String, returnEventFields: Set<String> = emptySet(), callback: (String) -> Unit): Element {
        val listenerId = parent.addEventListener(eventName, returnEventFields = returnEventFields, callback = callback)
        parent.creator?.onCleanup(true) {

        }
        return parent
    }

    inline fun <reified T : Any> event(eventName : String, eventType : KClass<T>, crossinline callback : (T)-> Unit) : Element {
        // TODO: Should probably cache this rather than do the reflection every time
        val eventPropertyNames = Companion.memberProperties(eventType)
        return event(eventName, eventPropertyNames, {propertiesAsString ->
            val props : T = gson.fromJson(propertiesAsString)
            try {
                callback(props)
            } catch (e : Exception) {
                logger.error(e, {"Exception thrown by callback in response to $eventName event"})
            }
        })
    }

    // Mouse Events
    fun click(callback: (MouseEvent) -> Unit) = event("click", eventType = MouseEvent::class, callback = callback)
    fun contextmenu(callback: (MouseEvent) -> Unit) = event("contextmenu", eventType = MouseEvent::class, callback = callback)
    fun dblclick(callback: (MouseEvent) -> Unit) = event("dblclick", eventType = MouseEvent::class, callback = callback)
    fun mousedown(callback: (MouseEvent) -> Unit) = event("mousedown", eventType = MouseEvent::class, callback = callback)
    fun mouseenter(callback: (MouseEvent) -> Unit) = event("mouseenter", eventType = MouseEvent::class, callback = callback)
    fun mouseleave(callback: (MouseEvent) -> Unit) = event("mouseleave", eventType = MouseEvent::class, callback = callback)
    fun mousemove(callback: (MouseEvent) -> Unit) = event("mousemove", eventType = MouseEvent::class, callback = callback)
    fun mouseover(callback: (MouseEvent) -> Unit) = event("mouseover", eventType = MouseEvent::class, callback = callback)
    fun mouseout(callback: (MouseEvent) -> Unit) = event("mouseout", eventType = MouseEvent::class, callback = callback)
    fun mouseup(callback: (MouseEvent) -> Unit) = event("mouseup", eventType = MouseEvent::class, callback = callback)

    // Keyboard Events
    fun keydown(callback: (KeyboardEvent) -> Unit) = event("keydown", eventType = KeyboardEvent::class, callback =  callback)
    fun keypress(callback: (KeyboardEvent) -> Unit) = event("keypress", eventType = KeyboardEvent::class, callback =  callback)
    fun keyup(callback: (KeyboardEvent) -> Unit) = event("keyup", eventType = KeyboardEvent::class, callback =  callback)

    // Frame / Object Events TODO: define eventtype
    fun abort(callback: (String) -> Unit) = event("abort", callback = callback)
    fun beforeunload(callback: (String) -> Unit) = event("beforeunload", callback = callback)
    fun error(callback: (String) -> Unit) = event("message", callback = callback)
    fun hashchange(callback: (String) -> Unit) = event("hashchange", callback = callback)
    fun load(callback: (String) -> Unit) = event("load", callback = callback)
    fun pageshow(callback: (String) -> Unit) = event("pageshow", callback = callback)
    fun pagehide(callback: (String) -> Unit) = event("pagehide", callback = callback)
    fun resize(callback: (String) -> Unit) = event("resize", callback = callback)
    fun scroll(callback: (String) -> Unit) = event("scroll", callback = callback)
    fun unload(callback: (String) -> Unit) = event("unload", callback = callback)

    // Form Events TODO: define eventtype
    fun blur(callback: (String) -> Unit) = event("blur", callback = callback)
    fun change(callback: (String) -> Unit) = event("change", callback = callback)
    fun focus(callback: (String) -> Unit) = event("focus", callback = callback)
    fun focusin(callback: (String) -> Unit) = event("focusin", callback = callback)
    fun focusout(callback: (String) -> Unit) = event("focusout", callback = callback)
    fun input(callback: (String) -> Unit) = event("input", callback = callback)
    fun invalid(callback: (String) -> Unit) = event("invalid", callback = callback)
    fun reset(callback: (String) -> Unit) = event("reset", callback = callback)
    fun search(callback: (String) -> Unit) = event("search", callback = callback)
    fun select(callback: (String) -> Unit) = event("select", callback = callback)
    fun submit(callback: (String) -> Unit) = event("submit", callback = callback)

    // Drag Events TODO: define eventtype
    fun drag(callback: (String) -> Unit) = event("drag", callback = callback)
    fun dragend(callback: (String) -> Unit) = event("dragend", callback = callback)
    fun dragenter(callback: (String) -> Unit) = event("dragenter", callback = callback)
    fun dragleave(callback: (String) -> Unit) = event("dragleave", callback = callback)
    fun dragover(callback: (String) -> Unit) = event("dragover", callback = callback)
    fun dragstart(callback: (String) -> Unit) = event("dragstart", callback = callback)
    fun drop(callback: (String) -> Unit) = event("drop", callback = callback)

    // Clipboard Events TODO: define eventtype
    fun copy(callback: (String) -> Unit) = event("copy", callback = callback)
    fun cut(callback: (String) -> Unit) = event("cut", callback = callback)
    fun paste(callback: (String) -> Unit) = event("paste", callback = callback)

    // Print Events TODO: define eventtype
    fun afterprint(callback: (String) -> Unit) = event("afterprint", callback = callback)
    fun beforeprint(callback: (String) -> Unit) = event("beforeprint", callback = callback)

    // Media Events TODO: define eventtype
    /*
    /** The event occurs when the loading of a media is aborted **/
    fun abort(callback: (String) -> Unit) = event("abort", callback = callback)
    /** The event occurs when the browser can start playing the media (when it has buffered enough to begin) **/
    fun canplay(callback: (String) -> Unit) = event("canplay", callback = callback)
    /** The event occurs when the browser can play through the media without stopping for buffering **/
    fun canplaythrough(callback: (String) -> Unit) = event("canplaythrough", callback = callback)
    /** The event occurs when the duration of the media is changed **/
    fun durationchange(callback: (String) -> Unit) = event("durationchange", callback = callback)
    /** The event occurs when something bad happens and the media file is suddenly unavailable (like unexpectedly disconnects) **/
    fun emptied(callback: (String) -> Unit) = event("emptied", callback = callback)
    /** The event occurs when the media has reach the end (useful for messages like "thanks for listening") **/
    fun ended(callback: (String) -> Unit) = event("ended", callback = callback)
    /** The event occurs when an message occurred during the loading of a media file **/
    fun message(callback: (String) -> Unit) = event("message", callback = callback)
    /** The event occurs when media data is loaded **/
    fun loadeddata(callback: (String) -> Unit) = event("loadeddata", callback = callback)
    /** The event occurs when meta data (like dimensions and duration) are loaded **/
    fun loadedmetadata(callback: (String) -> Unit) = event("loadedmetadata", callback = callback)
    /** The event occurs when the browser starts looking for the specified media **/
    fun loadstart(callback: (String) -> Unit) = event("loadstart", callback = callback)
    /** The event occurs when the media is paused either by the user or programmatically **/
    fun pause(callback: (String) -> Unit) = event("pause", callback = callback)
    /** The event occurs when the media has been started or is no longer paused **/
    fun play(callback: (String) -> Unit) = event("play", callback = callback)
    /** The event occurs when the media is playing after having been paused or stopped for buffering **/
    fun playing(callback: (String) -> Unit) = event("playing", callback = callback)
    /** The event occurs when the browser is in the process of getting the media data (downloading the media) **/
    fun progress(callback: (String) -> Unit) = event("progress", callback = callback)
    /** The event occurs when the playing speed of the media is changed **/
    fun ratechange(callback: (String) -> Unit) = event("ratechange", callback = callback)
    /** The event occurs when the user is finished moving/skipping to a new position in the media **/
    fun seeked(callback: (String) -> Unit) = event("seeked", callback = callback)
    /** The event occurs when the user starts moving/skipping to a new position in the media **/
    fun seeking(callback: (String) -> Unit) = event("seeking", callback = callback)
    /** The event occurs when the browser is trying to get media data, but data is not available **/
    fun stalled(callback: (String) -> Unit) = event("stalled", callback = callback)
    /** The event occurs when the browser is intentionally not getting media data **/
    fun suspend(callback: (String) -> Unit) = event("suspend", callback = callback)
    /** The event occurs when the playing position has changed (like when the user fast forwards to a different point in the media) **/
    fun timeupdate(callback: (String) -> Unit) = event("timeupdate", callback = callback)
    /** The event occurs when the volume of the media has changed (includes setting the volume to "mute") **/
    fun volumechange(callback: (String) -> Unit) = event("volumechange", callback = callback)
    /** The event occurs when the media has paused but is expected to resume (like when the media pauses to buffer more data) **/
    fun waiting(callback: (String) -> Unit) = event("waiting", callback = callback)
    */

    // Animation Events
    /** The event occurs when a CSS animation has completed **/
    fun animationend(callback: (AnimationEvent) -> Unit) = event("animationend", eventType = AnimationEvent::class, callback = callback)
    /** The event occurs when a CSS animation is repeated **/
    fun animationiteration(callback: (AnimationEvent) -> Unit) = event("animationiteration", eventType = AnimationEvent::class, callback = callback)
    /** The event occurs when a CSS animation has started **/
    fun animationstart(callback: (AnimationEvent) -> Unit) = event("animationstart", eventType = AnimationEvent::class, callback = callback)

    // Transition Events
    /** The event occurs when a CSS transition has completed **/
    fun transitionend(callback: (String) -> Unit) = event("transitionend", callback = callback)

    /** TODO: Add more to https://www.w3schools.com/jsref/dom_obj_event.asp
     * Missing:
     * * Servert-Sent Events
     * * Misc Events
     * * Touch Events
     * *
     */


    companion object {
        val memberPropertiesCache : ConcurrentHashMap<KClass<*>, Set<String>> = ConcurrentHashMap()
        inline fun <reified T:Any> memberProperties(clazz : KClass<T>) =
            memberPropertiesCache.get(clazz) ?:
            T::class.memberProperties.map {it.name}.toSet().also{ memberPropertiesCache.put(clazz,it) }
    }
}

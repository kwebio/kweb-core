package kweb.dom.element.events

import com.github.salomonbrys.kotson.fromJson
import kweb.*
import kweb.gson
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@KWebDSL
open class ONReceiver(internal val parent: Element, val retrieveJs: String? = null) : Element(parent) {

    val logger = KotlinLogging.logger {}

    open class Event(open val type : String, val retrieved : String?)

    data class MouseEvent(val type: String, val detail: Long,
                          val altKey : Boolean, val button : Int, val buttons : Int,
                          val clientX : Int, val clientY : Int, val ctrlKey : Boolean,
                          val metaKey : Boolean, val movementX : Int, val movementY : Int,
                          val region : String, val screenX : Int, val screenY : Int,
                          val shiftKey : Boolean, val x : Int = clientX, val y : Int = clientY, val retrieved : String?
    )

    data class KeyboardEvent(val type: String, val detail: Long,
                             val key : String, val altKey : Boolean,
                             val ctrlKey : Boolean, val code : String,
                             val location : Int, val metaKey : Boolean,
                             val shiftKey : Boolean, val locale : String,
                             val isComposing: Boolean, val retrieved : String?)

    data class AnimationEvent(val type: String, val detail: Long,
                              val animationName: String, val elapsedTime: Int, val retrieved : String?)

    fun event(eventName: String, returnEventFields: Set<String> = emptySet(), callback: (event: String) -> Unit): Element {
        parent.addEventListener(eventName, returnEventFields = returnEventFields, callback = {callback(it.toString())}, retrieveJs = retrieveJs)
        return parent
    }

    inline fun <reified T : Any> event(eventName: String, eventType: KClass<T>, crossinline callback: (event: T) -> Unit): Element {
        // TODO: Should probably cache this rather than do the reflection every time
        val eventPropertyNames = Companion.memberProperties(eventType)
        return event(eventName, eventPropertyNames) { propertiesAsString ->
            val props: T = gson.fromJson(propertiesAsString)
            try {
                callback(props)
            } catch (e : Exception) {
                logger.error(e) {"Exception thrown by callback in response to $eventName event"}
            }
        }
    }

    // Mouse Events
    fun click(callback: (event: MouseEvent) -> Unit) = event("click", eventType = MouseEvent::class, callback = callback)

    fun contextmenu(callback: (event: MouseEvent) -> Unit) = event("contextmenu", eventType = MouseEvent::class, callback = callback)
    fun dblclick(callback: (event: MouseEvent) -> Unit) = event("dblclick", eventType = MouseEvent::class, callback = callback)
    fun mousedown(callback: (event: MouseEvent) -> Unit) = event("mousedown", eventType = MouseEvent::class, callback = callback)
    fun mouseenter(callback: (event: MouseEvent) -> Unit) = event("mouseenter", eventType = MouseEvent::class, callback = callback)
    fun mouseleave(callback: (event: MouseEvent) -> Unit) = event("mouseleave", eventType = MouseEvent::class, callback = callback)
    fun mousemove(callback: (event: MouseEvent) -> Unit) = event("mousemove", eventType = MouseEvent::class, callback = callback)
    fun mouseover(callback: (event: MouseEvent) -> Unit) = event("mouseover", eventType = MouseEvent::class, callback = callback)
    fun mouseout(callback: (event: MouseEvent) -> Unit) = event("mouseout", eventType = MouseEvent::class, callback = callback)
    fun mouseup(callback: (event: MouseEvent) -> Unit) = event("mouseup", eventType = MouseEvent::class, callback = callback)

    // Keyboard Events
    fun keydown(callback: (event: KeyboardEvent) -> Unit) = event("keydown", eventType = KeyboardEvent::class, callback = callback)

    fun keypress(callback: (event: KeyboardEvent) -> Unit) = event("keypress", eventType = KeyboardEvent::class, callback = callback)
    fun keyup(callback: (event: KeyboardEvent) -> Unit) = event("keyup", eventType = KeyboardEvent::class, callback = callback)

    // Frame / Object Events TODO: define eventtype
    fun abort(callback: (event: Event) -> Unit) = event("abort", eventType = Event::class, callback = callback)

    fun beforeunload(callback: (event: Event) -> Unit) = event("beforeunload", eventType = Event::class, callback = callback)
    fun error(callback: (event: Event) -> Unit) = event("message", eventType = Event::class, callback = callback)
    fun hashchange(callback: (event: Event) -> Unit) = event("hashchange", eventType = Event::class, callback = callback)
    fun load(callback: (event: Event) -> Unit) = event("load", eventType = Event::class, callback = callback)
    fun pageshow(callback: (event: Event) -> Unit) = event("pageshow", eventType = Event::class, callback = callback)
    fun pagehide(callback: (event: Event) -> Unit) = event("pagehide", eventType = Event::class, callback = callback)
    fun resize(callback: (event: Event) -> Unit) = event("resize", eventType = Event::class, callback = callback)
    fun scroll(callback: (event: Event) -> Unit) = event("scroll", eventType = Event::class, callback = callback)
    fun unload(callback: (event: Event) -> Unit) = event("unload", eventType = Event::class, callback = callback)

    // Form Events TODO: define eventtypes
    fun blur(callback: (event: Event) -> Unit) = event("blur", eventType = Event::class, callback = callback)
    fun change(callback: (event: Event) -> Unit) = event("change", eventType = Event::class, callback = callback)
    fun focus(callback: (event: Event) -> Unit) = event("focus", eventType = Event::class, callback = callback)
    fun focusin(callback: (event: Event) -> Unit) = event("focusin", eventType = Event::class, callback = callback)
    fun focusout(callback: (event: Event) -> Unit) = event("focusout", eventType = Event::class, callback = callback)
    fun input(callback: (event: Event) -> Unit) = event("input", eventType = Event::class, callback = callback)
    fun invalid(callback: (event: Event) -> Unit) = event("invalid", eventType = Event::class, callback = callback)
    fun reset(callback: (event: Event) -> Unit) = event("reset", eventType = Event::class, callback = callback)
    fun search(callback: (event: Event) -> Unit) = event("search", eventType = Event::class, callback = callback)
    fun select(callback: (event: Event) -> Unit) = event("select", eventType = Event::class, callback = callback)
    fun submit(callback: (event: Event) -> Unit) = event("submit", eventType = Event::class, callback = callback)

    // Drag Events TODO: define eventtype
    fun drag(callback: (event: Event) -> Unit) = event("drag", eventType = Event::class, callback = callback)

    fun dragend(callback: (event: Event) -> Unit) = event("dragend", eventType = Event::class, callback = callback)
    fun dragenter(callback: (event: Event) -> Unit) = event("dragenter", eventType = Event::class, callback = callback)
    fun dragleave(callback: (event: Event) -> Unit) = event("dragleave", eventType = Event::class, callback = callback)
    fun dragover(callback: (event: Event) -> Unit) = event("dragover", eventType = Event::class, callback = callback)
    fun dragstart(callback: (event: Event) -> Unit) = event("dragstart", eventType = Event::class, callback = callback)
    fun drop(callback: (event: Event) -> Unit) = event("drop", eventType = Event::class, callback = callback)

    // Clipboard Events TODO: define eventtype
    fun copy(callback: (event: Event) -> Unit) = event("copy", eventType = Event::class, callback = callback)

    fun cut(callback: (event: Event) -> Unit) = event("cut", eventType = Event::class, callback = callback)
    fun paste(callback: (event: Event) -> Unit) = event("paste", eventType = Event::class, callback = callback)

    // Print Events TODO: define eventtype
    fun afterprint(callback: (event: Event) -> Unit) = event("afterprint", eventType = Event::class, callback = callback)

    fun beforeprint(callback: (event: Event) -> Unit) = event("beforeprint", eventType = Event::class, callback = callback)

    // Media Events TODO: define eventtype
    /*
    /** The event occurs when the loading of a media is aborted **/
    fun abort(callback: (event : String) -> Unit) = event("abort", eventType = Event::class, callback = callback)
    /** The event occurs when the browser can start playing the media (when it has buffered enough to begin) **/
    fun canplay(callback: (event : String) -> Unit) = event("canplay", eventType = Event::class, callback = callback)
    /** The event occurs when the browser can play through the media without stopping for buffering **/
    fun canplaythrough(callback: (event : String) -> Unit) = event("canplaythrough", eventType = Event::class, callback = callback)
    /** The event occurs when the duration of the media is changed **/
    fun durationchange(callback: (event : String) -> Unit) = event("durationchange", eventType = Event::class, callback = callback)
    /** The event occurs when something bad happens and the media file is suddenly unavailable (like unexpectedly disconnects) **/
    fun emptied(callback: (event : String) -> Unit) = event("emptied", eventType = Event::class, callback = callback)
    /** The event occurs when the media has reach the end (useful for messages like "thanks for listening") **/
    fun ended(callback: (event : String) -> Unit) = event("ended", eventType = Event::class, callback = callback)
    /** The event occurs when an message occurred during the loading of a media file **/
    fun message(callback: (event : String) -> Unit) = event("message", eventType = Event::class, callback = callback)
    /** The event occurs when media data is loaded **/
    fun loadeddata(callback: (event : String) -> Unit) = event("loadeddata", eventType = Event::class, callback = callback)
    /** The event occurs when meta data (like dimensions and duration) are loaded **/
    fun loadedmetadata(callback: (event : String) -> Unit) = event("loadedmetadata", eventType = Event::class, callback = callback)
    /** The event occurs when the browser starts looking for the specified media **/
    fun loadstart(callback: (event : String) -> Unit) = event("loadstart", eventType = Event::class, callback = callback)
    /** The event occurs when the media is paused either by the user or programmatically **/
    fun pause(callback: (event : String) -> Unit) = event("pause", eventType = Event::class, callback = callback)
    /** The event occurs when the media has been started or is no longer paused **/
    fun play(callback: (event : String) -> Unit) = event("play", eventType = Event::class, callback = callback)
    /** The event occurs when the media is playing after having been paused or stopped for buffering **/
    fun playing(callback: (event : String) -> Unit) = event("playing", eventType = Event::class, callback = callback)
    /** The event occurs when the browser is in the process of getting the media data (downloading the media) **/
    fun progress(callback: (event : String) -> Unit) = event("progress", eventType = Event::class, callback = callback)
    /** The event occurs when the playing speed of the media is changed **/
    fun ratechange(callback: (event : String) -> Unit) = event("ratechange", eventType = Event::class, callback = callback)
    /** The event occurs when the user is finished moving/skipping to a new position in the media **/
    fun seeked(callback: (event : String) -> Unit) = event("seeked", eventType = Event::class, callback = callback)
    /** The event occurs when the user starts moving/skipping to a new position in the media **/
    fun seeking(callback: (event : String) -> Unit) = event("seeking", eventType = Event::class, callback = callback)
    /** The event occurs when the browser is trying to get media data, but data is not available **/
    fun stalled(callback: (event : String) -> Unit) = event("stalled", eventType = Event::class, callback = callback)
    /** The event occurs when the browser is intentionally not getting media data **/
    fun suspend(callback: (event : String) -> Unit) = event("suspend", eventType = Event::class, callback = callback)
    /** The event occurs when the playing position has changed (like when the user fast forwards to a different point in the media) **/
    fun timeupdate(callback: (event : String) -> Unit) = event("timeupdate", eventType = Event::class, callback = callback)
    /** The event occurs when the volume of the media has changed (includes setting the volume to "mute") **/
    fun volumechange(callback: (event : String) -> Unit) = event("volumechange", eventType = Event::class, callback = callback)
    /** The event occurs when the media has paused but is expected to resume (like when the media pauses to buffer more data) **/
    fun waiting(callback: (event : String) -> Unit) = event("waiting", eventType = Event::class, callback = callback)
    */

    // Animation Events
    /** The event occurs when a CSS animation has completed **/
    fun animationend(callback: (event: AnimationEvent) -> Unit) = event("animationend", eventType = AnimationEvent::class, callback = callback)
    /** The event occurs when a CSS animation is repeated **/
    fun animationiteration(callback: (event: AnimationEvent) -> Unit) = event("animationiteration", eventType = AnimationEvent::class, callback = callback)
    /** The event occurs when a CSS animation has started **/
    fun animationstart(callback: (event: AnimationEvent) -> Unit) = event("animationstart", eventType = AnimationEvent::class, callback = callback)

    // Transition Events
    /** The event occurs when a CSS transition has completed **/
    fun transitionend(callback: (event: Event) -> Unit) = event("transitionend", eventType = Event::class, callback = callback)

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

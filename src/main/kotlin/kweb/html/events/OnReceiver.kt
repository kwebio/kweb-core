package kweb.html.events

import com.github.salomonbrys.kotson.fromJson
import kweb.util.KWebDSL
import kweb.util.gson
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@PublishedApi internal val logger = KotlinLogging.logger {}

@KWebDSL
class OnReceiver<T : EventGenerator<T>>(internal val source: T, private val retrieveJs: String? = null) {

    fun event(eventName: String, returnEventFields: Set<String> = emptySet(), callback: (event: String) -> Unit): T {
        source.addEventListener(eventName, returnEventFields = returnEventFields, callback = { callback(it.toString()) }, retrieveJs = retrieveJs)
        return source
    }

    inline fun <reified U : Any> event(eventName: String, eventType: KClass<U>, crossinline callback: (event: U) -> Unit): T {
        // TODO: Should probably cache this rather than do the reflection every time
        val eventPropertyNames = memberProperties(eventType)
        return event(eventName, eventPropertyNames) { propertiesAsString ->
            val props: U = gson.fromJson(propertiesAsString)
            try {
                callback(props)
            } catch (e: Exception) {
                logger.error(e) { "Exception thrown by callback in response to $eventName event" }
            }
        }
    }

    companion object {
        val memberPropertiesCache: ConcurrentHashMap<KClass<*>, Set<String>> = ConcurrentHashMap()
        inline fun <reified T : Any> memberProperties(clazz: KClass<T>) =
                memberPropertiesCache.get(clazz)
                        ?: T::class.memberProperties.map { it.name }.toSet().also { memberPropertiesCache.put(clazz, it) }
    }

    // Mouse events
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

    // Keyboard events
    fun keydown(callback: (event: KeyboardEvent) -> Unit) = event("keydown", eventType = KeyboardEvent::class, callback = callback)
    fun keypress(callback: (event: KeyboardEvent) -> Unit) = event("keypress", eventType = KeyboardEvent::class, callback = callback)
    fun keyup(callback: (event: KeyboardEvent) -> Unit) = event("keyup", eventType = KeyboardEvent::class, callback = callback)

    // Focus Events TODO: define event types
    // https://www.w3schools.com/jsref/obj_focusevent.asp
    fun blur(callback: (event: Event) -> Unit) = event("blur", eventType = Event::class, callback = callback)
    fun focus(callback: (event: Event) -> Unit) = event("focus", eventType = Event::class, callback = callback)
    fun focusin(callback: (event: Event) -> Unit) = event("focusin", eventType = Event::class, callback = callback)
    fun focusout(callback: (event: Event) -> Unit) = event("focusout", eventType = Event::class, callback = callback)

    // Frame / Object Events TODO: define eventtype
    fun abort(callback: (event: Event) -> Unit) = event("abort", eventType = Event::class, callback = callback)
    fun beforeunload(callback: (event: Event) -> Unit) = event("beforeunload", eventType = Event::class, callback = callback)
    fun error(callback: (event: Event) -> Unit) = event("error", eventType = Event::class, callback = callback)
    fun hashchange(callback: (event: Event) -> Unit) = event("hashchange", eventType = Event::class, callback = callback)
    fun load(callback: (event: Event) -> Unit) = event("load", eventType = Event::class, callback = callback)
    fun pageshow(callback: (event: Event) -> Unit) = event("pageshow", eventType = Event::class, callback = callback)
    fun pagehide(callback: (event: Event) -> Unit) = event("pagehide", eventType = Event::class, callback = callback)
    fun resize(callback: (event: Event) -> Unit) = event("resize", eventType = Event::class, callback = callback)
    fun scroll(callback: (event: Event) -> Unit) = event("scroll", eventType = Event::class, callback = callback)
    fun unload(callback: (event: Event) -> Unit) = event("unload", eventType = Event::class, callback = callback)

    // Form Events TODO: define eventtypes
    fun change(callback: (event: Event) -> Unit) = event("change", eventType = Event::class, callback = callback)
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

    // Selection Events TODO: define eventtype
    fun selectstart(callback: (event: Event) -> Unit) = event("selectstart", eventType = Event::class, callback = callback)
    fun selectionchange(callback: (event: Event) -> Unit) = event("selectionchange", eventType = Event::class, callback = callback)

    /*
    // Media Events TODO: define eventtype
    fun abort(callback: (event: Event) -> Unit) = event("abort", eventType = Event::class, callback = callback)
    /** The event occurs when the browser can start playing the media (when it has buffered enough to begin) **/
    fun canplay(callback: (event: Event) -> Unit) = event("canplay", eventType = Event::class, callback = callback)
    /** The event occurs when the browser can play through the media without stopping for buffering **/
    fun canplaythrough(callback: (event: Event) -> Unit) = event("canplaythrough", eventType = Event::class, callback = callback)
    /** The event occurs when the duration of the media is changed **/
    fun durationchange(callback: (event: Event) -> Unit) = event("durationchange", eventType = Event::class, callback = callback)
    /** The event occurs when something bad happens and the media file is suddenly unavailable (like unexpectedly disconnects) **/
    fun emptied(callback: (event: Event) -> Unit) = event("emptied", eventType = Event::class, callback = callback)
    /** The event occurs when the media has reach the end (useful for messages like "thanks for listening") **/
    fun ended(callback: (event: Event) -> Unit) = event("ended", eventType = Event::class, callback = callback)
    /** The event occurs when an message occurred during the loading of a media file **/
    fun message(callback: (event: Event) -> Unit) = event("message", eventType = Event::class, callback = callback)
    /** The event occurs when media data is loaded **/
    fun loadeddata(callback: (event: Event) -> Unit) = event("loadeddata", eventType = Event::class, callback = callback)
    /** The event occurs when meta data (like dimensions and duration) are loaded **/
    fun loadedmetadata(callback: (event: Event) -> Unit) = event("loadedmetadata", eventType = Event::class, callback = callback)
    /** The event occurs when the browser starts looking for the specified media **/
    fun loadstart(callback: (event: Event) -> Unit) = event("loadstart", eventType = Event::class, callback = callback) /** The event occurs when the media is paused either by the user or programmatically **/
    fun pause(callback: (event: Event) -> Unit) = event("pause", eventType = Event::class, callback = callback)
    /** The event occurs when the media has been started or is no longer paused **/
    fun play(callback: (event: Event) -> Unit) = event("play", eventType = Event::class, callback = callback)
    /** The event occurs when the media is playing after having been paused or stopped for buffering **/
    fun playing(callback: (event: Event) -> Unit) = event("playing", eventType = Event::class, callback = callback)
    /** The event occurs when the browser is in the process of getting the media data (downloading the media) **/
    fun progress(callback: (event: Event) -> Unit) = event("progress", eventType = Event::class, callback = callback)
    /** The event occurs when the playing speed of the media is changed **/
    fun ratechange(callback: (event: Event) -> Unit) = event("ratechange", eventType = Event::class, callback = callback)
    /** The event occurs when the user is finished moving/skipping to a new position in the media **/
    fun seeked(callback: (event: Event) -> Unit) = event("seeked", eventType = Event::class, callback = callback)
    /** The event occurs when the user starts moving/skipping to a new position in the media **/
    fun seeking(callback: (event: Event) -> Unit) = event("seeking", eventType = Event::class, callback = callback)
    /** The event occurs when the browser is trying to get media data, but data is not available **/
    fun stalled(callback: (event: Event) -> Unit) = event("stalled", eventType = Event::class, callback = callback)
    /** The event occurs when the browser is intentionally not getting media data **/
    fun suspend(callback: (event: Event) -> Unit) = event("suspend", eventType = Event::class, callback = callback)
    /** The event occurs when the playing position has changed (like when the user fast forwards to a different point in the media) **/
    fun timeupdate(callback: (event: Event) -> Unit) = event("timeupdate", eventType = Event::class, callback = callback)
    /** The event occurs when the volume of the media has changed (includes setting the volume to "mute") **/
    fun volumechange(callback: (event: Event) -> Unit) = event("volumechange", eventType = Event::class, callback = callback)
    /** The event occurs when the media has paused but is expected to resume (like when the media pauses to buffer more data) **/
    fun waiting(callback: (event: Event) -> Unit) = event("waiting", eventType = Event::class, callback = callback)
    */

    /** TODO: Add more to https://www.w3schools.com/jsref/dom_obj_event.asp
     * Missing:
     * * Server-Sent Events
     * * Misc Events
     * * Touch Events
     * *
     */

}
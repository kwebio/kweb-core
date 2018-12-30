package io.kweb.dom.element.events

import io.kweb.dom.element.*
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * See [here](https://docs.kweb.io/en/latest/dom.html#immediate-events).
 */
@KWebDSL
open class ONImmediateReceiver(internal val parent: Element) : Element(parent) {

    val logger = KotlinLogging.logger {}

    fun event(eventName: String, callback: () -> Unit): Element {
        val immediateJS = parent.browser.kweb.catchOutbound {
            callback()
        }
        parent.addImmediateEventCode(eventName, immediateJS.joinToString(separator = ""))
        return parent
    }

    // Mouse Events
    fun click(callback: () -> Unit) = event("click", callback = callback)

    fun contextmenu(callback: () -> Unit) = event("contextmenu", callback = callback)
    fun dblclick(callback: () -> Unit) = event("dblclick", callback = callback)
    fun mousedown(callback: () -> Unit) = event("mousedown", callback = callback)
    fun mouseenter(callback: () -> Unit) = event("mouseenter", callback = callback)
    fun mouseleave(callback: () -> Unit) = event("mouseleave", callback = callback)
    fun mousemove(callback: () -> Unit) = event("mousemove", callback = callback)
    fun mouseover(callback: () -> Unit) = event("mouseover", callback = callback)
    fun mouseout(callback: () -> Unit) = event("mouseout", callback = callback)
    fun mouseup(callback: () -> Unit) = event("mouseup", callback = callback)

    // Keyboard Events
    fun keydown(callback: () -> Unit) = event("keydown", callback = callback)

    fun keypress(callback: () -> Unit) = event("keypress", callback = callback)
    fun keyup(callback: () -> Unit) = event("keyup", callback = callback)

    // Frame / Object Events TODO: define eventtype
    fun abort(callback: () -> Unit) = event("abort", callback = callback)

    fun beforeunload(callback: () -> Unit) = event("beforeunload", callback = callback)
    fun error(callback: () -> Unit) = event("message", callback = callback)
    fun hashchange(callback: () -> Unit) = event("hashchange", callback = callback)
    fun load(callback: () -> Unit) = event("load", callback = callback)
    fun pageshow(callback: () -> Unit) = event("pageshow", callback = callback)
    fun pagehide(callback: () -> Unit) = event("pagehide", callback = callback)
    fun resize(callback: () -> Unit) = event("resize", callback = callback)
    fun scroll(callback: () -> Unit) = event("scroll", callback = callback)
    fun unload(callback: () -> Unit) = event("unload", callback = callback)

    // Form Events TODO: define eventtype
    fun blur(callback: () -> Unit) = event("blur", callback = callback)

    fun change(callback: () -> Unit) = event("change", callback = callback)
    fun focus(callback: () -> Unit) = event("focus", callback = callback)
    fun focusin(callback: () -> Unit) = event("focusin", callback = callback)
    fun focusout(callback: () -> Unit) = event("focusout", callback = callback)
    fun input(callback: () -> Unit) = event("input", callback = callback)
    fun invalid(callback: () -> Unit) = event("invalid", callback = callback)
    fun reset(callback: () -> Unit) = event("reset", callback = callback)
    fun search(callback: () -> Unit) = event("search", callback = callback)
    fun select(callback: () -> Unit) = event("select", callback = callback)
    fun submit(callback: () -> Unit) = event("submit", callback = callback)

    // Drag Events TODO: define eventtype
    fun drag(callback: () -> Unit) = event("drag", callback = callback)

    fun dragend(callback: () -> Unit) = event("dragend", callback = callback)
    fun dragenter(callback: () -> Unit) = event("dragenter", callback = callback)
    fun dragleave(callback: () -> Unit) = event("dragleave", callback = callback)
    fun dragover(callback: () -> Unit) = event("dragover", callback = callback)
    fun dragstart(callback: () -> Unit) = event("dragstart", callback = callback)
    fun drop(callback: () -> Unit) = event("drop", callback = callback)

    // Clipboard Events TODO: define eventtype
    fun copy(callback: () -> Unit) = event("copy", callback = callback)

    fun cut(callback: () -> Unit) = event("cut", callback = callback)
    fun paste(callback: () -> Unit) = event("paste", callback = callback)

    // Print Events TODO: define eventtype
    fun afterprint(callback: () -> Unit) = event("afterprint", callback = callback)

    fun beforeprint(callback: () -> Unit) = event("beforeprint", callback = callback)

    // Media Events TODO: define eventtype
    /*
    /** The event occurs when the loading of a media is aborted **/
    fun abort(callback: () -> Unit) = event("abort", callback = callback)
    /** The event occurs when the browser can start playing the media (when it has buffered enough to begin) **/
    fun canplay(callback: () -> Unit) = event("canplay", callback = callback)
    /** The event occurs when the browser can play through the media without stopping for buffering **/
    fun canplaythrough(callback: () -> Unit) = event("canplaythrough", callback = callback)
    /** The event occurs when the duration of the media is changed **/
    fun durationchange(callback: () -> Unit) = event("durationchange", callback = callback)
    /** The event occurs when something bad happens and the media file is suddenly unavailable (like unexpectedly disconnects) **/
    fun emptied(callback: () -> Unit) = event("emptied", callback = callback)
    /** The event occurs when the media has reach the end (useful for messages like "thanks for listening") **/
    fun ended(callback: () -> Unit) = event("ended", callback = callback)
    /** The event occurs when an message occurred during the loading of a media file **/
    fun message(callback: () -> Unit) = event("message", callback = callback)
    /** The event occurs when media data is loaded **/
    fun loadeddata(callback: () -> Unit) = event("loadeddata", callback = callback)
    /** The event occurs when meta data (like dimensions and duration) are loaded **/
    fun loadedmetadata(callback: () -> Unit) = event("loadedmetadata", callback = callback)
    /** The event occurs when the browser starts looking for the specified media **/
    fun loadstart(callback: () -> Unit) = event("loadstart", callback = callback)
    /** The event occurs when the media is paused either by the user or programmatically **/
    fun pause(callback: () -> Unit) = event("pause", callback = callback)
    /** The event occurs when the media has been started or is no longer paused **/
    fun play(callback: () -> Unit) = event("play", callback = callback)
    /** The event occurs when the media is playing after having been paused or stopped for buffering **/
    fun playing(callback: () -> Unit) = event("playing", callback = callback)
    /** The event occurs when the browser is in the process of getting the media data (downloading the media) **/
    fun progress(callback: () -> Unit) = event("progress", callback = callback)
    /** The event occurs when the playing speed of the media is changed **/
    fun ratechange(callback: () -> Unit) = event("ratechange", callback = callback)
    /** The event occurs when the user is finished moving/skipping to a new position in the media **/
    fun seeked(callback: () -> Unit) = event("seeked", callback = callback)
    /** The event occurs when the user starts moving/skipping to a new position in the media **/
    fun seeking(callback: () -> Unit) = event("seeking", callback = callback)
    /** The event occurs when the browser is trying to get media data, but data is not available **/
    fun stalled(callback: () -> Unit) = event("stalled", callback = callback)
    /** The event occurs when the browser is intentionally not getting media data **/
    fun suspend(callback: () -> Unit) = event("suspend", callback = callback)
    /** The event occurs when the playing position has changed (like when the user fast forwards to a different point in the media) **/
    fun timeupdate(callback: () -> Unit) = event("timeupdate", callback = callback)
    /** The event occurs when the volume of the media has changed (includes setting the volume to "mute") **/
    fun volumechange(callback: () -> Unit) = event("volumechange", callback = callback)
    /** The event occurs when the media has paused but is expected to resume (like when the media pauses to buffer more data) **/
    fun waiting(callback: () -> Unit) = event("waiting", callback = callback)
    */

    // Animation Events
    /** The event occurs when a CSS animation has completed **/
    fun animationend(callback: () -> Unit) = event("animationend", callback = callback)

    /** The event occurs when a CSS animation is repeated **/
    fun animationiteration(callback: () -> Unit) = event("animationiteration", callback = callback)

    /** The event occurs when a CSS animation has started **/
    fun animationstart(callback: () -> Unit) = event("animationstart", callback = callback)

    // Transition Events
    /** The event occurs when a CSS transition has completed **/
    fun transitionend(callback: () -> Unit) = event("transitionend", callback = callback)

    /** TODO: Add more to https://www.w3schools.com/jsref/dom_obj_event.asp
     * Missing:
     * * Servert-Sent Events
     * * Misc Events
     * * Touch Events
     * *
     */

    companion object {

        val memberPropertiesCache: ConcurrentHashMap<KClass<*>, Set<String>> = ConcurrentHashMap()
        inline fun <reified T : Any> memberProperties(clazz: KClass<T>) =
                memberPropertiesCache.get(clazz)
                        ?: T::class.memberProperties.map { it.name }.toSet().also { memberPropertiesCache.put(clazz, it) }
    }
}

package kweb.html.events

import kotlinx.coroutines.flow.asFlow
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import kweb.WebBrowser
import kweb.util.KWebDSL
import mu.KotlinLogging
import java.util.Collections.emptySet
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@PublishedApi internal val logger = KotlinLogging.logger {}

@KWebDSL
class OnReceiver<T : EventGenerator<T>>(val source: T, private val retrieveJs: String? = null, val preventDefault : Boolean) {

    fun event(eventName: String, returnEventFields: Set<String> = emptySet(), callback: (event: JsonElement) -> Unit): T {
        source.addEventListener(eventName, returnEventFields = returnEventFields,
                callback = { callback(it) }, retrieveJs = retrieveJs, preventDefault = preventDefault)
        return source
    }

    inline fun <reified U : Any> event(eventName: String, crossinline callback: (event: U) -> Unit): T {
        // TODO [$635f2be19f96970007b1807b]: Should probably cache this rather than do the reflection every time
        val eventPropertyNames = memberProperties(U::class)

        val serializer = serializer<U>()
        return event(eventName, eventPropertyNames) { propertiesAsElement ->
            if (propertiesAsElement == JsonNull) {
                /*
                 * Couldn't figure out why this was happening, but it doesn't appear to have any
                 * negative effect. TODO: Figure out why it's happening and fix
                 */
                logger.warn { "Received event callback with JsonNull where data is expected, disregarding" }
            } else {
                val props = Json.decodeFromJsonElement(serializer, propertiesAsElement)
                try {
                    if (source.browser.isCatchingOutbound() == null) {
                        source.browser.batch(WebBrowser.CatcherType.EVENT) {
                            callback(props)
                        }
                    } else {
                        callback(props)
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Exception thrown by callback in response to $eventName event" }
                }
            }
        }
    }

    companion object {
        val memberPropertiesCache: ConcurrentHashMap<KClass<*>, Set<String>> = ConcurrentHashMap()
        inline fun <reified T : Any> memberProperties(clazz: KClass<T>) =
                memberPropertiesCache[clazz]
                        ?: T::class.memberProperties.map { it.name }.toSet().minus("retrieved").also { memberPropertiesCache.put(clazz, it) }
    }

    // Mouse events
    fun click(callback: (event: MouseEvent) -> Unit) = event("click", callback = callback)
    fun contextmenu(callback: (event: MouseEvent) -> Unit) = event("contextmenu", callback = callback)
    fun dblclick(callback: (event: MouseEvent) -> Unit) = event("dblclick", callback = callback)
    fun mousedown(callback: (event: MouseEvent) -> Unit) = event("mousedown", callback = callback)
    fun mouseenter(callback: (event: MouseEvent) -> Unit) = event("mouseenter", callback = callback)
    fun mouseleave(callback: (event: MouseEvent) -> Unit) = event("mouseleave", callback = callback)
    fun mousemove(callback: (event: MouseEvent) -> Unit) = event("mousemove", callback = callback)
    fun mouseover(callback: (event: MouseEvent) -> Unit) = event("mouseover", callback = callback)
    fun mouseout(callback: (event: MouseEvent) -> Unit) = event("mouseout", callback = callback)
    fun mouseup(callback: (event: MouseEvent) -> Unit) = event("mouseup", callback = callback)

    // Keyboard events
    fun keydown(callback: (event: KeyboardEvent) -> Unit) = event("keydown", callback = callback)
    fun keypress(callback: (event: KeyboardEvent) -> Unit) = event("keypress", callback = callback)
    fun keyup(callback: (event: KeyboardEvent) -> Unit) = event("keyup", callback = callback)

    // Focus Events TODO: define event types
    // https://www.w3schools.com/jsref/obj_focusevent.asp
    fun blur(callback: (event: Event) -> Unit) = event("blur",  callback = callback)
    fun focus(callback: (event: Event) -> Unit) = event("focus",  callback = callback)
    fun focusin(callback: (event: Event) -> Unit) = event("focusin",  callback = callback)
    fun focusout(callback: (event: Event) -> Unit) = event("focusout",  callback = callback)

    // Frame / Object Events TODO: define eventtype
    fun abort(callback: (event: Event) -> Unit) = event("abort",  callback = callback)
    fun beforeunload(callback: (event: Event) -> Unit) = event("beforeunload",  callback = callback)
    fun error(callback: (event: Event) -> Unit) = event("error",  callback = callback)
    fun hashchange(callback: (event: Event) -> Unit) = event("hashchange",  callback = callback)
    fun load(callback: (event: Event) -> Unit) = event("load",  callback = callback)
    fun pageshow(callback: (event: Event) -> Unit) = event("pageshow",  callback = callback)
    fun pagehide(callback: (event: Event) -> Unit) = event("pagehide",  callback = callback)
    fun resize(callback: (event: Event) -> Unit) = event("resize",  callback = callback)
    fun scroll(callback: (event: Event) -> Unit) = event("scroll",  callback = callback)
    fun unload(callback: (event: Event) -> Unit) = event("unload",  callback = callback)

    // Form Events TODO: define eventtypes
    fun change(callback: (event: Event) -> Unit) = event("change",  callback = callback)
    fun input(callback: (event: Event) -> Unit) = event("input",  callback = callback)
    fun invalid(callback: (event: Event) -> Unit) = event("invalid",  callback = callback)
    fun reset(callback: (event: Event) -> Unit) = event("reset",  callback = callback)
    fun search(callback: (event: Event) -> Unit) = event("search",  callback = callback)
    fun select(callback: (event: Event) -> Unit) = event("select",  callback = callback)
    fun submit(callback: (event: Event) -> Unit) = event("submit",  callback = callback)

    // Drag Events TODO: define eventtype
    fun drag(callback: (event: Event) -> Unit) = event("drag",  callback = callback)
    fun dragend(callback: (event: Event) -> Unit) = event("dragend",  callback = callback)
    fun dragenter(callback: (event: Event) -> Unit) = event("dragenter",  callback = callback)
    fun dragleave(callback: (event: Event) -> Unit) = event("dragleave",  callback = callback)
    fun dragover(callback: (event: Event) -> Unit) = event("dragover",  callback = callback)
    fun dragstart(callback: (event: Event) -> Unit) = event("dragstart",  callback = callback)
    fun drop(callback: (event: Event) -> Unit) = event("drop",  callback = callback)

    // Clipboard Events TODO: define eventtype
    fun copy(callback: (event: Event) -> Unit) = event("copy",  callback = callback)
    fun cut(callback: (event: Event) -> Unit) = event("cut",  callback = callback)
    fun paste(callback: (event: Event) -> Unit) = event("paste",  callback = callback)

    // Print Events TODO: define eventtype
    fun afterprint(callback: (event: Event) -> Unit) = event("afterprint",  callback = callback)
    fun beforeprint(callback: (event: Event) -> Unit) = event("beforeprint",  callback = callback)

    // Selection Events TODO: define eventtype
    fun selectstart(callback: (event: Event) -> Unit) = event("selectstart",  callback = callback)
    fun selectionchange(callback: (event: Event) -> Unit) = event("selectionchange",  callback = callback)

    // Window events
    fun popstate(callback: (event: Event) -> Unit) = event("popstate",  callback = callback)


    /*
    // Media Events TODO: define eventtype
    fun abort(callback: (event: Event) -> Unit) = event("abort",  callback = callback)
    /** The event occurs when the browser can start playing the media (when it has buffered enough to begin) **/
    fun canplay(callback: (event: Event) -> Unit) = event("canplay",  callback = callback)
    /** The event occurs when the browser can play through the media without stopping for buffering **/
    fun canplaythrough(callback: (event: Event) -> Unit) = event("canplaythrough",  callback = callback)
    /** The event occurs when the duration of the media is changed **/
    fun durationchange(callback: (event: Event) -> Unit) = event("durationchange",  callback = callback)
    /** The event occurs when something bad happens and the media file is suddenly unavailable (like unexpectedly disconnects) **/
    fun emptied(callback: (event: Event) -> Unit) = event("emptied",  callback = callback)
    /** The event occurs when the media has reach the end (useful for messages like "thanks for listening") **/
    fun ended(callback: (event: Event) -> Unit) = event("ended",  callback = callback)
    /** The event occurs when an message occurred during the loading of a media file **/
    fun message(callback: (event: Event) -> Unit) = event("message",  callback = callback)
    /** The event occurs when media data is loaded **/
    fun loadeddata(callback: (event: Event) -> Unit) = event("loadeddata",  callback = callback)
    /** The event occurs when meta data (like dimensions and duration) are loaded **/
    fun loadedmetadata(callback: (event: Event) -> Unit) = event("loadedmetadata",  callback = callback)
    /** The event occurs when the browser starts looking for the specified media **/
    fun loadstart(callback: (event: Event) -> Unit) = event("loadstart",  callback = callback) /** The event occurs when the media is paused either by the user or programmatically **/
    fun pause(callback: (event: Event) -> Unit) = event("pause",  callback = callback)
    /** The event occurs when the media has been started or is no longer paused **/
    fun play(callback: (event: Event) -> Unit) = event("play",  callback = callback)
    /** The event occurs when the media is playing after having been paused or stopped for buffering **/
    fun playing(callback: (event: Event) -> Unit) = event("playing",  callback = callback)
    /** The event occurs when the browser is in the process of getting the media data (downloading the media) **/
    fun progress(callback: (event: Event) -> Unit) = event("progress",  callback = callback)
    /** The event occurs when the playing speed of the media is changed **/
    fun ratechange(callback: (event: Event) -> Unit) = event("ratechange",  callback = callback)
    /** The event occurs when the user is finished moving/skipping to a new position in the media **/
    fun seeked(callback: (event: Event) -> Unit) = event("seeked",  callback = callback)
    /** The event occurs when the user starts moving/skipping to a new position in the media **/
    fun seeking(callback: (event: Event) -> Unit) = event("seeking",  callback = callback)
    /** The event occurs when the browser is trying to get media data, but data is not available **/
    fun stalled(callback: (event: Event) -> Unit) = event("stalled",  callback = callback)
    /** The event occurs when the browser is intentionally not getting media data **/
    fun suspend(callback: (event: Event) -> Unit) = event("suspend",  callback = callback)
    /** The event occurs when the playing position has changed (like when the user fast forwards to a different point in the media) **/
    fun timeupdate(callback: (event: Event) -> Unit) = event("timeupdate",  callback = callback)
    /** The event occurs when the volume of the media has changed (includes setting the volume to "mute") **/
    fun volumechange(callback: (event: Event) -> Unit) = event("volumechange",  callback = callback)
    /** The event occurs when the media has paused but is expected to resume (like when the media pauses to buffer more data) **/
    fun waiting(callback: (event: Event) -> Unit) = event("waiting",  callback = callback)
    */

    /** TODO [$635f2be19f96970007b1807c]: Add more to https://www.w3schools.com/jsref/dom_obj_event.asp
     * Missing:
     * * Server-Sent Events
     * * Misc Events
     * * Touch Events
     * *
     */

}
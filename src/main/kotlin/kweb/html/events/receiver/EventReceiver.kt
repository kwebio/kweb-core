package kweb.html.events.receiver

interface EventReceiver

open class Event(open val type: String, val retrieved: String?)

// Focus Events TODO: define event types
// https://www.w3schools.com/jsref/obj_focusevent.asp
fun <ON, T> ON.blur(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("blur", eventType = Event::class, callback = callback)
fun <ON, T> ON.focus(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("focus", eventType = Event::class, callback = callback)
fun <ON, T> ON.focusin(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("focusin", eventType = Event::class, callback = callback)
fun <ON, T> ON.focusout(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("focusout", eventType = Event::class, callback = callback)


// Frame / Object Events TODO: define eventtype
fun <ON, T> ON.abort(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("abort", eventType = Event::class, callback = callback)

fun <ON, T> ON.beforeunload(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("beforeunload", eventType = Event::class, callback = callback)
fun <ON, T> ON.error(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("error", eventType = Event::class, callback = callback)
fun <ON, T> ON.hashchange(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("hashchange", eventType = Event::class, callback = callback)
fun <ON, T> ON.load(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("load", eventType = Event::class, callback = callback)
fun <ON, T> ON.pageshow(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("pageshow", eventType = Event::class, callback = callback)
fun <ON, T> ON.pagehide(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("pagehide", eventType = Event::class, callback = callback)
fun <ON, T> ON.resize(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("resize", eventType = Event::class, callback = callback)
fun <ON, T> ON.scroll(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("scroll", eventType = Event::class, callback = callback)
fun <ON, T> ON.unload(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("unload", eventType = Event::class, callback = callback)


// Form Events TODO: define eventtypes
fun <ON, T> ON.change(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("change", eventType = Event::class, callback = callback)
fun <ON, T> ON.input(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("input", eventType = Event::class, callback = callback)
fun <ON, T> ON.invalid(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("invalid", eventType = Event::class, callback = callback)
fun <ON, T> ON.reset(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("reset", eventType = Event::class, callback = callback)
fun <ON, T> ON.search(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("search", eventType = Event::class, callback = callback)
fun <ON, T> ON.select(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("select", eventType = Event::class, callback = callback)
fun <ON, T> ON.submit(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("submit", eventType = Event::class, callback = callback)

// Drag Events TODO: define eventtype
fun <ON, T> ON.drag(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("drag", eventType = Event::class, callback = callback)

fun <ON, T> ON.dragend(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("dragend", eventType = Event::class, callback = callback)
fun <ON, T> ON.dragenter(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("dragenter", eventType = Event::class, callback = callback)
fun <ON, T> ON.dragleave(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("dragleave", eventType = Event::class, callback = callback)
fun <ON, T> ON.dragover(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("dragover", eventType = Event::class, callback = callback)
fun <ON, T> ON.dragstart(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("dragstart", eventType = Event::class, callback = callback)
fun <ON, T> ON.drop(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("drop", eventType = Event::class, callback = callback)

// Clipboard Events TODO: define eventtype
fun <ON, T> ON.copy(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("copy", eventType = Event::class, callback = callback)

fun <ON, T> ON.cut(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("cut", eventType = Event::class, callback = callback)
fun <ON, T> ON.paste(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("paste", eventType = Event::class, callback = callback)

// Print Events TODO: define eventtype
fun <ON, T> ON.afterprint(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("afterprint", eventType = Event::class, callback = callback)

fun <ON, T> ON.beforeprint(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("beforeprint", eventType = Event::class, callback = callback)

// Media Events TODO: define eventtype
/*
/** The event occurs when the loading of a media is aborted **/
fun <ON, T> ON.abort(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("abort", eventType = Event::class, callback = callback)
/** The event occurs when the browser can start playing the media (when it has buffered enough to begin) **/
fun <ON, T> ON.canplay(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("canplay", eventType = Event::class, callback = callback)
/** The event occurs when the browser can play through the media without stopping for buffering **/
fun <ON, T> ON.canplaythrough(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("canplaythrough", eventType = Event::class, callback = callback)
/** The event occurs when the duration of the media is changed **/
fun <ON, T> ON.durationchange(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("durationchange", eventType = Event::class, callback = callback)
/** The event occurs when something bad happens and the media file is suddenly unavailable (like unexpectedly disconnects) **/
fun <ON, T> ON.emptied(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("emptied", eventType = Event::class, callback = callback)
/** The event occurs when the media has reach the end (useful for messages like "thanks for listening") **/
fun <ON, T> ON.ended(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("ended", eventType = Event::class, callback = callback)
/** The event occurs when an message occurred during the loading of a media file **/
fun <ON, T> ON.message(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("message", eventType = Event::class, callback = callback)
/** The event occurs when media data is loaded **/
fun <ON, T> ON.loadeddata(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("loadeddata", eventType = Event::class, callback = callback)
/** The event occurs when meta data (like dimensions and duration) are loaded **/
fun <ON, T> ON.loadedmetadata(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("loadedmetadata", eventType = Event::class, callback = callback)
/** The event occurs when the browser starts looking for the specified media **/
fun <ON, T> ON.loadstart(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("loadstart", eventType = Event::class, callback = callback)
/** The event occurs when the media is paused either by the user or programmatically **/
fun <ON, T> ON.pause(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("pause", eventType = Event::class, callback = callback)
/** The event occurs when the media has been started or is no longer paused **/
fun <ON, T> ON.play(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("play", eventType = Event::class, callback = callback)
/** The event occurs when the media is playing after having been paused or stopped for buffering **/
fun <ON, T> ON.playing(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("playing", eventType = Event::class, callback = callback)
/** The event occurs when the browser is in the process of getting the media data (downloading the media) **/
fun <ON, T> ON.progress(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("progress", eventType = Event::class, callback = callback)
/** The event occurs when the playing speed of the media is changed **/
fun <ON, T> ON.ratechange(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("ratechange", eventType = Event::class, callback = callback)
/** The event occurs when the user is finished moving/skipping to a new position in the media **/
fun <ON, T> ON.seeked(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("seeked", eventType = Event::class, callback = callback)
/** The event occurs when the user starts moving/skipping to a new position in the media **/
fun <ON, T> ON.seeking(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("seeking", eventType = Event::class, callback = callback)
/** The event occurs when the browser is trying to get media data, but data is not available **/
fun <ON, T> ON.stalled(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("stalled", eventType = Event::class, callback = callback)
/** The event occurs when the browser is intentionally not getting media data **/
fun <ON, T> ON.suspend(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("suspend", eventType = Event::class, callback = callback)
/** The event occurs when the playing position has changed (like when the user fast forwards to a different point in the media) **/
fun <ON, T> ON.timeupdate(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("timeupdate", eventType = Event::class, callback = callback)
/** The event occurs when the volume of the media has changed (includes setting the volume to "mute") **/
fun <ON, T> ON.volumechange(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("volumechange", eventType = Event::class, callback = callback)
/** The event occurs when the media has paused but is expected to resume (like when the media pauses to buffer more data) **/
fun <ON, T> ON.waiting(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("waiting", eventType = Event::class, callback = callback)
*/

// Animation Events (https://www.w3schools.com/jsref/obj_animationevent.asp)
/** The event occurs when a CSS animation has completed **/
fun <ON, T> ON.animationend(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("animationend", eventType = Event::class, callback = callback)

/** The event occurs when a CSS animation is repeated **/
fun <ON, T> ON.animationiteration(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("animationiteration", eventType = Event::class, callback = callback)

/** The event occurs when a CSS animation has started **/
fun <ON, T> ON.animationstart(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("animationstart", eventType = Event::class, callback = callback)

// Transition Events
/** The event occurs when a CSS transition has completed **/
fun <ON, T> ON.transitionend(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: EventReceiver =
        event("transitionend", eventType = Event::class, callback = callback)

/** TODO: Add more to https://www.w3schools.com/jsref/dom_obj_event.asp
 * Missing:
 * * Server-Sent Events
 * * Misc Events
 * * Touch Events
 * *
 */



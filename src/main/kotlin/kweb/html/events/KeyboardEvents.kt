package kweb.html.events

import kweb.dom.element.events.ONReceiver

interface KeyboardEvents

fun <ON, T> ON.keypress(callback: (event: ONReceiver.KeyboardEvent) -> Unit) where ON: NewOnReceiver<T>, T: KeyboardEvents =
    event("keypress", eventType = ONReceiver.KeyboardEvent::class, callback = callback)

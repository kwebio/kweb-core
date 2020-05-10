package kweb.html.events

import kweb.dom.element.events.ONReceiver

interface MouseEvents

fun <ON, T> ON.click(callback: (event: ONReceiver.MouseEvent) -> Unit) where ON: NewOnReceiver<T>, T: KeyboardEvents =
        event("click", eventType = ONReceiver.MouseEvent::class, callback = callback)

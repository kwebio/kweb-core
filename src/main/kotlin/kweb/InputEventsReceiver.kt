package kweb

import kweb.html.events.Event
import kweb.html.events.OnImmediateReceiver
import kweb.html.events.OnReceiver

interface InputEventsReceiver

// TODO: define event type
fun <ON, T> ON.input(callback: (event: Event) -> Unit) where ON: OnReceiver<T>, T: InputEventsReceiver =
        event("input", eventType = Event::class, callback = callback)
fun <ION, T> ION.input(callback: () -> Unit) where ION: OnImmediateReceiver<T>, T: InputEventsReceiver = event("input", callback)

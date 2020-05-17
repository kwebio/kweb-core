package kweb.html.events.receiver

interface InputEventsReceiver

// TODO: define event type
fun <ON, T> ON.input(callback: (event: Event) -> Unit) where ON: NewOnReceiver<T>, T: InputEventsReceiver =
        event("input", eventType = Event::class, callback = callback)

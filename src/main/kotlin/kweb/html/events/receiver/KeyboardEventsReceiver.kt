package kweb.html.events.receiver

interface KeyboardEventsReceiver

data class KeyboardEvent(val type: String, val detail: Long,
                         val key: String, val altKey: Boolean,
                         val ctrlKey: Boolean, val code: String,
                         val location: Int, val metaKey: Boolean,
                         val shiftKey: Boolean, val locale: String,
                         val isComposing: Boolean, val retrieved: String?)

fun <ON, T> ON.keydown(callback: (event: KeyboardEvent) -> Unit) where ON: NewOnReceiver<T>, T: KeyboardEventsReceiver =
        event("keydown", eventType = KeyboardEvent::class, callback = callback)

fun <ON, T> ON.keypress(callback: (event: KeyboardEvent) -> Unit) where ON: NewOnReceiver<T>, T: KeyboardEventsReceiver =
        event("keypress", eventType = KeyboardEvent::class, callback = callback)

fun <ON, T> ON.keyup(callback: (event: KeyboardEvent) -> Unit) where ON: NewOnReceiver<T>, T: KeyboardEventsReceiver =
        event("keyup", eventType = KeyboardEvent::class, callback = callback)

package kweb.html.events

interface KeyboardEventReceiver

data class KeyboardEvent(val type: String, val detail: Long,
                         val key: String, val altKey: Boolean,
                         val ctrlKey: Boolean, val code: String,
                         val location: Int, val metaKey: Boolean,
                         val shiftKey: Boolean, val locale: String,
                         val isComposing: Boolean, val retrieved: String?)

fun <ON, T> ON.keydown(callback: (event: KeyboardEvent) -> Unit) where ON: NewOnReceiver<T>, T: KeyboardEventReceiver =
        event("keydown", eventType = KeyboardEvent::class, callback = callback)
fun <ION, T> ION.keydown(callback: () -> Unit) where ION: NewOnImmediateReceiver<T>, T: KeyboardEventReceiver = event("keydown", callback)

fun <ON, T> ON.keypress(callback: (event: KeyboardEvent) -> Unit) where ON: NewOnReceiver<T>, T: KeyboardEventReceiver =
        event("keypress", eventType = KeyboardEvent::class, callback = callback)
fun <ION, T> ION.keypress(callback: () -> Unit) where ION: NewOnImmediateReceiver<T>, T: KeyboardEventReceiver = event("keypress", callback)

fun <ON, T> ON.keyup(callback: (event: KeyboardEvent) -> Unit) where ON: NewOnReceiver<T>, T: KeyboardEventReceiver =
        event("keyup", eventType = KeyboardEvent::class, callback = callback)
fun <ION, T> ION.keyup(callback: () -> Unit) where ION: NewOnImmediateReceiver<T>, T: KeyboardEventReceiver = event("keyup", callback)

package kweb.html.events.receiver

interface MouseEventsReceiver

data class MouseEvent(val type: String, val detail: Long,
                      val altKey: Boolean, val button: Int, val buttons: Int,
                      val clientX: Int, val clientY: Int, val ctrlKey: Boolean,
                      val metaKey: Boolean, val movementX: Int, val movementY: Int,
                      val region: String, val screenX: Int, val screenY: Int,
                      val shiftKey: Boolean, val x: Int = clientX, val y: Int = clientY, val retrieved: String?
)

fun <ON, T> ON.click(callback: (event: MouseEvent) -> Unit) where ON: NewOnReceiver<T>, T: MouseEventsReceiver =
        event("click", eventType = MouseEvent::class, callback = callback)

fun <ON, T> ON.contextmenu(callback: (event: MouseEvent) -> Unit) where ON: NewOnReceiver<T>, T: MouseEventsReceiver =
        event("contextmenu", eventType = MouseEvent::class, callback = callback)

fun <ON, T> ON.dblclick(callback: (event: MouseEvent) -> Unit) where ON: NewOnReceiver<T>, T: MouseEventsReceiver =
        event("dblclick", eventType = MouseEvent::class, callback = callback)

fun <ON, T> ON.mousedown(callback: (event: MouseEvent) -> Unit) where ON: NewOnReceiver<T>, T: MouseEventsReceiver =
        event("mousedown", eventType = MouseEvent::class, callback = callback)

fun <ON, T> ON.mouseenter(callback: (event: MouseEvent) -> Unit) where ON: NewOnReceiver<T>, T: MouseEventsReceiver =
        event("mouseenter", eventType = MouseEvent::class, callback = callback)

fun <ON, T> ON.mouseleave(callback: (event: MouseEvent) -> Unit) where ON: NewOnReceiver<T>, T: MouseEventsReceiver =
        event("mouseleave", eventType = MouseEvent::class, callback = callback)

fun <ON, T> ON.mousemove(callback: (event: MouseEvent) -> Unit) where ON: NewOnReceiver<T>, T: MouseEventsReceiver =
        event("mousemove", eventType = MouseEvent::class, callback = callback)

fun <ON, T> ON.mouseover(callback: (event: MouseEvent) -> Unit) where ON: NewOnReceiver<T>, T: MouseEventsReceiver =
        event("mouseover", eventType = MouseEvent::class, callback = callback)

fun <ON, T> ON.mouseout(callback: (event: MouseEvent) -> Unit) where ON: NewOnReceiver<T>, T: MouseEventsReceiver =
        event("mouseout", eventType = MouseEvent::class, callback = callback)

fun <ON, T> ON.mouseup(callback: (event: MouseEvent) -> Unit) where ON: NewOnReceiver<T>, T: MouseEventsReceiver =
        event("mouseup", eventType = MouseEvent::class, callback = callback)


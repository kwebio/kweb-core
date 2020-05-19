package kweb

import kweb.html.events.OnImmediateReceiver
import kweb.html.events.OnReceiver

interface MouseEventReceiver

data class MouseEvent(val type: String, val detail: Long,
                      val altKey: Boolean, val button: Int, val buttons: Int,
                      val clientX: Int, val clientY: Int, val ctrlKey: Boolean,
                      val metaKey: Boolean, val movementX: Int, val movementY: Int,
                      val region: String, val screenX: Int, val screenY: Int,
                      val shiftKey: Boolean, val x: Int = clientX, val y: Int = clientY, val retrieved: String?
)

fun <ON, T> ON.click(callback: (event: MouseEvent) -> Unit) where ON: OnReceiver<T>, T: MouseEventReceiver =
        event("click", eventType = MouseEvent::class, callback = callback)
fun <ION, T> ION.click(callback: () -> Unit) where ION: OnImmediateReceiver<T>, T: MouseEventReceiver =
        event("click", callback = callback)

fun <ON, T> ON.contextmenu(callback: (event: MouseEvent) -> Unit) where ON: OnReceiver<T>, T: MouseEventReceiver =
        event("contextmenu", eventType = MouseEvent::class, callback = callback)
fun <ION, T> ION.contextmenu(callback: () -> Unit) where ION: OnImmediateReceiver<T>, T: EventReceiver = event("contextmenu", callback)

fun <ON, T> ON.dblclick(callback: (event: MouseEvent) -> Unit) where ON: OnReceiver<T>, T: MouseEventReceiver =
        event("dblclick", eventType = MouseEvent::class, callback = callback)
fun <ION, T> ION.dblclick(callback: () -> Unit) where ION: OnImmediateReceiver<T>, T: EventReceiver = event("dblclick", callback)

fun <ON, T> ON.mousedown(callback: (event: MouseEvent) -> Unit) where ON: OnReceiver<T>, T: MouseEventReceiver =
        event("mousedown", eventType = MouseEvent::class, callback = callback)
fun <ION, T> ION.mousedown(callback: () -> Unit) where ION: OnImmediateReceiver<T>, T: EventReceiver = event("mousedown", callback)

fun <ON, T> ON.mouseenter(callback: (event: MouseEvent) -> Unit) where ON: OnReceiver<T>, T: MouseEventReceiver =
        event("mouseenter", eventType = MouseEvent::class, callback = callback)
fun <ION, T> ION.mouseenter(callback: () -> Unit) where ION: OnImmediateReceiver<T>, T: EventReceiver = event("mouseenter", callback)

fun <ON, T> ON.mouseleave(callback: (event: MouseEvent) -> Unit) where ON: OnReceiver<T>, T: MouseEventReceiver =
        event("mouseleave", eventType = MouseEvent::class, callback = callback)
fun <ION, T> ION.mouseleave(callback: () -> Unit) where ION: OnImmediateReceiver<T>, T: EventReceiver = event("mouseleave", callback)

fun <ON, T> ON.mousemove(callback: (event: MouseEvent) -> Unit) where ON: OnReceiver<T>, T: MouseEventReceiver =
        event("mousemove", eventType = MouseEvent::class, callback = callback)
fun <ION, T> ION.mousemove(callback: () -> Unit) where ION: OnImmediateReceiver<T>, T: EventReceiver = event("mousemove", callback)

fun <ON, T> ON.mouseover(callback: (event: MouseEvent) -> Unit) where ON: OnReceiver<T>, T: MouseEventReceiver =
        event("mouseover", eventType = MouseEvent::class, callback = callback)
fun <ION, T> ION.mouseover(callback: () -> Unit) where ION: OnImmediateReceiver<T>, T: EventReceiver = event("mouseover", callback)

fun <ON, T> ON.mouseout(callback: (event: MouseEvent) -> Unit) where ON: OnReceiver<T>, T: MouseEventReceiver =
        event("mouseout", eventType = MouseEvent::class, callback = callback)
fun <ION, T> ION.mouseout(callback: () -> Unit) where ION: OnImmediateReceiver<T>, T: EventReceiver = event("mouseout", callback)

fun <ON, T> ON.mouseup(callback: (event: MouseEvent) -> Unit) where ON: OnReceiver<T>, T: MouseEventReceiver =
        event("mouseup", eventType = MouseEvent::class, callback = callback)
fun <ION, T> ION.mouseup(callback: () -> Unit) where ION: OnImmediateReceiver<T>, T: EventReceiver = event("mouseup", callback)


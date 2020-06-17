package kweb.html.events

open class Event(open val type: String, val retrieved: String?)

data class KeyboardEvent(val type: String, val detail: Long,
                         val key: String, val altKey: Boolean,
                         val ctrlKey: Boolean, val code: String,
                         val location: Int, val metaKey: Boolean,
                         val shiftKey: Boolean, val locale: String,
                         val isComposing: Boolean, val retrieved: String?)

data class MouseEvent(val type: String, val detail: Long,
                      val altKey: Boolean, val button: Int, val buttons: Int,
                      val clientX: Int, val clientY: Int, val ctrlKey: Boolean,
                      val metaKey: Boolean, val movementX: Int, val movementY: Int,
                      val region: String, val screenX: Int, val screenY: Int,
                      val shiftKey: Boolean, val x: Int = clientX, val y: Int = clientY, val retrieved: String?)


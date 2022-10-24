package kweb.html.events

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * Corresponds to a [JavaScript event](https://developer.mozilla.org/en-US/docs/Web/API/Event) object.
 */
@Serializable
data class Event(
    val type: String,
    /** @see kweb.Element.on **/
    val retrieved: JsonElement = JsonNull
)

/**
 * Corresponds to a [JavaScript event](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent) object.
 */
@Serializable
data class KeyboardEvent(
    val type: String,
    val detail: Long,
    val key: String,
    val altKey: Boolean,
    val ctrlKey: Boolean,
    val code: String,
    val location: Int,
    val metaKey: Boolean,
    val shiftKey: Boolean,
    val locale: String? = null,
    val isComposing: Boolean,
    /** @see kweb.Element.on **/
    val retrieved: JsonElement = JsonNull
)

/**
 * Corresponds to a [JavaScript event](https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent) object.
 */
@Serializable
data class MouseEvent(
    val type: String,
    val detail: Long,
    val altKey: Boolean,
    val button: Int,
    val buttons: Int,
    val clientX: Float,
    val clientY: Float,
    val ctrlKey: Boolean,
    val metaKey: Boolean,
    val movementX: Int? = null,
    val movementY: Int? = null,
    val region: String? = null,
    val screenX: Int,
    val screenY: Int,
    val shiftKey: Boolean,
    val x: Float = clientX,
    val y: Float = clientY,
    /** If `elementX.on(retrieveJs = "...") was provided, this will contain the value returned by the
     * JavaScript expression.  If no `retrieveJs` was specified it will be JsonNull.
     **/
    val retrieved: JsonElement = JsonNull
)


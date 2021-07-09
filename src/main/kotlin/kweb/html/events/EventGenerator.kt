package kweb.html.events

import kotlinx.serialization.json.JsonElement
import kweb.WebBrowser
import java.util.*

/**
 * Something that event listeners can be attached to, such as an [kweb.Element] or a [kweb.html.Document]
 */

// TODO: Code for both event listener functions should be generalized
//       and lifted into this class, paying close attention to
//       any differences between the different implementations.
interface EventGenerator<T> {
    val browser: WebBrowser
    fun addImmediateEventCode(eventName: String, jsCode: String)
    fun addEventListener(eventName: String, returnEventFields: Set<String> = Collections.emptySet(), retrieveJs: String?, callback: (JsonElement) -> Unit): T
}
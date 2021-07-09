package kweb.html.events

import kotlinx.serialization.json.JsonElement
import kweb.WebBrowser
import java.util.*

/**
 * Something that event listeners can be attached to, such as an [kweb.Element] or a [kweb.html.Document]
 */
interface EventGenerator<T> {
    val browser: WebBrowser
    fun addImmediateEventCode(eventName: String, jsCode: String)
    fun addEventListener(eventName: String, returnEventFields: Set<String> = Collections.emptySet(), retrieveJs: String?, callback: (JsonElement) -> Unit): T
}
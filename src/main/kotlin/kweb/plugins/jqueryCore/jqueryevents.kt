package kweb.plugins.jqueryCore

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import kweb.html.events.MouseEvent
import kweb.util.random
import java.util.*
import kotlin.reflect.full.memberProperties

/**
 * Created by ian on 2/22/17.
 */

open class JQueryOnReceiver(val parent: JQueryReceiver) {
    fun event(event: String, returnEventFields: Set<String> = Collections.emptySet(), callback: (JsonElement) -> Unit): JQueryReceiver {
        val callbackId = Math.abs(random.nextInt())
        val eventObject = "{" + returnEventFields.map { "\"$it\" : event.$it" }.joinToString(separator = ", ") + "}"
        val js = "${parent.selectorExpression}.on(${JsonPrimitive(event)}, function(event) {callbackWs({}, $eventObject);})"
        parent.webBrowser.callJsFunctionWithCallback(js, callbackId, callback = { payload ->
            callback.invoke(payload)
        }, JsonPrimitive(callbackId))
        return parent
    }

    inline fun <reified T : Any> event(eventName: String, crossinline callback: (T) -> Unit): JQueryReceiver {
        // TODO [$635f2be19f96970007b1807d]: Should probably cache this rather than do the reflection every time
        val eventPropertyNames = T::class.memberProperties.map { it.name }.toSet()

        val deserializer = serializer<T>()
        event(eventName, eventPropertyNames) { propertiesAsElement ->
            val props: T = Json.decodeFromJsonElement(deserializer, propertiesAsElement)
            callback(props)
        }
        return parent
    }

    // From http://www.w3schools.com/jquery/jquery_ref_events.asp, incomplete
    fun blur(callback: (MouseEvent) -> Unit) = event("blur", callback = callback)
    fun click(callback: (MouseEvent) -> Unit) = event("click", callback = callback)
    fun dblclick(callback: (MouseEvent) -> Unit) = event("dblclick", callback = callback)
    fun focus(callback: (MouseEvent) -> Unit) = event("focus", callback = callback)
    fun focusin(callback: (MouseEvent) -> Unit) = event("focusin", callback = callback)
    fun focusout(callback: (MouseEvent) -> Unit) = event("focusout", callback = callback)
    fun hover(callback: (MouseEvent) -> Unit) = event("hover", callback = callback)
    fun mouseup(callback: (MouseEvent) -> Unit) = event("mouseup", callback = callback)
    fun mousedown(callback: (MouseEvent) -> Unit) = event("mousedown", callback = callback)
    fun mouseenter(callback: (MouseEvent) -> Unit) = event("mouseenter", callback = callback)
    fun mouseleave(callback: (MouseEvent) -> Unit) = event("mouseleave", callback = callback)
    fun mousemove(callback: (MouseEvent) -> Unit) = event("mousemove", callback = callback)
}
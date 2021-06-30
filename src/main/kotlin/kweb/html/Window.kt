package kweb.html

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kweb.*
import kweb.html.events.Event
import kweb.html.events.EventGenerator
import kweb.html.events.OnImmediateReceiver
import kweb.html.events.OnReceiver
import kweb.util.random

/**
 * Represents the in-browser Document Object Model, corresponding to the JavaScript
 * [document](https://www.w3schools.com/jsref/dom_obj_document.asp) object.
 *
 * Passed in as `doc` to the `buildPage` lambda of the [Kweb] constructor.
 *
 * @sample document_sample
 */
class Window(val receiver: WebBrowser) : EventGenerator<Window> {

    override val browser = receiver

    override fun addImmediateEventCode(eventName: String, jsCode: String) {
        val wrappedJS = """
            return window.addEventListener({}, function(event) {
                $jsCode
            });
        """.trimIndent()
        GlobalScope.launch {
            receiver.callJsFunctionWithResult(wrappedJS, JsonPrimitive(eventName))
        }
    }


    override fun addEventListener(eventName: String, returnEventFields: Set<String>, retrieveJs: String?, callback: (JsonElement) -> Unit): Window {
        val callbackId = Math.abs(random.nextInt())
        val retrieveJs = if (retrieveJs != null) ", \"retrieved\" : ($retrieveJs)" else ""
        val eventObject = "{" + returnEventFields.joinToString(separator = ", ") { "\"$it\" : event.$it" } + retrieveJs + "}"
        val js = """
            window.addEventListener({}, function(event) {
                callbackWs({}, $eventObject);
            });
        """
        receiver.callJsFunctionWithCallback(js, callbackId, callback = { payload ->
            callback.invoke(payload)
        }, JsonPrimitive(eventName), JsonPrimitive(callbackId))
        return this
    }

    /**
     * See [here](https://docs.kweb.io/en/latest/dom.html#listening-for-events).
     */
    val on: OnReceiver<Window> get() = OnReceiver(this)

    /**
     * You can supply a javascript expression `retrieveJs` which will
     * be available via [Event.retrieved]
     */
    fun on(retrieveJs: String) = OnReceiver(this, retrieveJs)

    /**
     * See [here](https://docs.kweb.io/en/latest/dom.html#immediate-events).
     */
    val onImmediate get() = OnImmediateReceiver(this)
}
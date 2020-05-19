package kweb.html

import com.github.salomonbrys.kotson.toJson
import kweb.*
import kweb.html.events.*
import kweb.util.random

/**
 * Represents the in-browser Document Object Model, corresponding to the JavaScript
 * [document](https://www.w3schools.com/jsref/dom_obj_document.asp) object.
 *
 * Passed in as `doc` to the `buildPage` lambda of the [Kweb] constructor.
 *
 * @sample document_sample
 */
class Document(val receiver: WebBrowser) : EventGenerator<Document>, KeyboardEventReceiver, MouseEventReceiver, EventReceiver {
    fun getElementById(id: String) = Element(receiver, null, "document.getElementById(\"$id\")", id = id)

    val cookie = CookieReceiver(receiver)

    val body = BodyElement(receiver)

    val head = HeadElement(receiver)

    val origin = receiver.evaluate("document.origin")

    fun execCommand(command: String) {
        receiver.execute("document.execCommand(\"$command\");")
    }

    /**
     * Allows data to be stored in and retrieved from the browser's [local storage](https://www.w3schools.com/html/html5_webstorage.as).
     *
     * @sample local_storage_sample
     */
    val localStorage get() = StorageReceiver(receiver, StorageType.local)


    /**
     * Allows data to be stored in and retrieved from the browser's [session storage](https://www.w3schools.com/html/html5_webstorage.as).
     */
    val sessionStorage get() = StorageReceiver(receiver, StorageType.session)

    override val browser = receiver

    override fun addImmediateEventCode(eventName: String, jsCode: String) {
        val wrappedJS = """
            document.addEventListener(${eventName.toJson()}, function(event) {
                $jsCode
            });
        """.trimIndent()
        receiver.evaluate(wrappedJS)
    }


    override fun addEventListener(eventName: String, returnEventFields: Set<String>, retrieveJs: String?, callback: (Any) -> Unit): Document {
        val callbackId = Math.abs(random.nextInt())
        val retrieveJs = if (retrieveJs != null) ", \"retrieved\" : ($retrieveJs)" else ""
        val eventObject = "{" + returnEventFields.map { "\"$it\" : event.$it" }.joinToString(separator = ", ") + retrieveJs + "}"
        val js = """
            document.addEventListener(${eventName.toJson()}, function(event) {
                callbackWs($callbackId, $eventObject);
            });
        """
        receiver.executeWithCallback(js, callbackId) { payload ->
            callback.invoke(payload)
        }
        return this
    }

    /**
     * See [here](https://docs.kweb.io/en/latest/dom.html#listening-for-events).
     */
    val on: OnReceiver<Document> get() = OnReceiver(this)

    /**
     * You can supply a javascript expression `retrieveJs` which will
     * be available via [Event.retrieveJs]
     */
    fun on(retrieveJs: String) = OnReceiver(this, retrieveJs)

    /**
     * See [here](https://docs.kweb.io/en/latest/dom.html#immediate-events).
     */
    val onImmediate get() = OnImmediateReceiver(this)
}

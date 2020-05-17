package kweb.html

import com.github.salomonbrys.kotson.toJson
import kweb.*
import kweb.dom.element.storage.StorageReceiver
import kweb.dom.element.storage.StorageType
import kweb.html.events.receiver.EventGenerator
import kweb.html.events.receiver.NewOnReceiver

/**
 * Represents the in-browser Document Object Model, corresponding to the JavaScript
 * [document](https://www.w3schools.com/jsref/dom_obj_document.asp) object.
 *
 * Passed in as `doc` to the `buildPage` lambda of the [Kweb] constructor.
 *
 * @sample document_sample
 */
class Document(val receiver: WebBrowser) : EventGenerator<Document> {
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

    val on: NewOnReceiver<Document> get() = NewOnReceiver(this)
}

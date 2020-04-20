package kweb.html

import kweb.*
import kweb.dom.element.storage.StorageReceiver
import kweb.dom.element.storage.StorageType

/**
 * Represents the in-browser Document Object Model, corresponding to the JavaScript
 * [document](https://www.w3schools.com/jsref/dom_obj_document.asp) object.
 *
 * Passed in as `doc` to the `buildPage` lambda of the [Kweb] constructor.
 *
 * @sample document_sample
 */
class Document(val receiver: WebBrowser) {
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
}

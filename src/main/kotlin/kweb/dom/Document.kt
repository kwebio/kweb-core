package kweb.dom

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

    fun execCommand(command : String) {
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


/**
 * Represents the `body` element of the in-browser Document Object Model, corresponding to
 * the JavaScript [body](https://www.w3schools.com/tags/tag_body.asp) tag.
 *
 * @sample document_sample
 */
class BodyElement(webBrowser: WebBrowser, id: String? = null) : Element(webBrowser, null, "document.body", "body", id)

class HeadElement(webBrowser: WebBrowser, id: String? = null) : Element(webBrowser, null, "document.head", "head", id)

open class TitleElement(parent: Element) : Element(parent)
fun ElementCreator<HeadElement>.title(attributes: Map<String, Any> = attr) = TitleElement(element("title", attributes))


private fun document_sample() {
    Kweb(port = 1234, buildPage = {
        doc.body.new {
            h1().text("Hello World!")
        }
    })
}


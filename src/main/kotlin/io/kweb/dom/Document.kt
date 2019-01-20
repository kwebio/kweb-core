package io.kweb.dom

import io.kweb.*
import io.kweb.dom.cookies.CookieReceiver
import io.kweb.dom.element.*
import io.kweb.dom.element.creation.tags.h1

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

    val origin = receiver.evaluate("document.origin")

    fun execCommand(command : String) {
        receiver.execute("document.execCommand(\"$command\");")
    }
}


/**
 * Represents the `body` element of the in-browser Document Object Model, corresponding to
 * the JavaScript [body](https://www.w3schools.com/tags/tag_body.asp) tag.
 *
 * @sample document_sample
 */
class BodyElement(webBrowser: WebBrowser, id: String? = null) : Element(webBrowser, null, "document.body", "body", id)

private fun document_sample() {
    Kweb(port = 1234) {
        doc.body.new {
            h1().text("Hello World!")
        }
    }
}
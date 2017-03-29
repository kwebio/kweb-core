package com.github.sanity.kweb.dom

import com.github.sanity.kweb.Kweb
import com.github.sanity.kweb.WebBrowser
import com.github.sanity.kweb.dom.cookies.CookieReceiver
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.tags.h1
import com.github.sanity.kweb.dom.element.modification.text
import com.github.sanity.kweb.dom.element.new

/**
 * Represents the in-browser Document Object Model, corresponding to the JavaScript
 * [document](https://www.w3schools.com/jsref/dom_obj_document.asp) object.
 *
 * Passed in as `doc` to the `buildPage` lambda of the [Kweb] constructor.
 *
 * @sample document_sample
 */
class Document(val receiver: WebBrowser) {
    fun getElementById(id: String) = Element(receiver, "document.getElementById(\"$id\")")

    val cookie = CookieReceiver(receiver)

    val body = BodyElement(receiver)
}

/**
 * Represents the `body` element of the in-browser Document Object Model, corresponding to
 * the JavaScript [body](https://www.w3schools.com/tags/tag_body.asp) tag.
 *
 * @sample document_sample
 */
class BodyElement(webBrowser: WebBrowser, id: String? = null) : Element(webBrowser, "document.body", "body", id)

private fun document_sample() {
    Kweb(port = 1234) {
        doc.body.new {
            h1().text("Hello World!")
        }
    }
}
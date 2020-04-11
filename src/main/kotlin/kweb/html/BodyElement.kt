package kweb.html

import kweb.Element
import kweb.WebBrowser

/**
 * Represents the `body` element of the in-browser Document Object Model, corresponding to
 * the JavaScript [body](https://www.w3schools.com/tags/tag_body.asp) tag.
 *
 * @sample document_sample
 */
class BodyElement(webBrowser: WebBrowser, id: String? = null) : Element(webBrowser, null, "document.body", "body", id)
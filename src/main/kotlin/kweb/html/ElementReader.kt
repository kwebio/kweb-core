package kweb.html

import kweb.Element
import kweb.WebBrowser
import kweb.util.KWebDSL

@KWebDSL
open class ElementReader(protected val receiver: WebBrowser, internal val elementId: String) {
    constructor(element: Element) : this(element.browser, element.id)

    init {
        require(!receiver.kweb.isCatchingOutbound()) {
            """
            Reading the DOM when an outboundMessageCatcher is set is likely to have unintended consequences.
            Most likely you are trying to read the DOM within an `immediatelyOn {...}` block.
        """.trimIndent()
        }
    }

    suspend fun getTagName(): String {
        return receiver.callJsFunctionWithResult(
            "return document.getElementById({}).tagName", elementId).toString()
    }

    suspend fun getAttributes(): Map<String, Any> {
        return receiver.callJsFunctionWithResult(
            "return document.getElementById({}).attributes", elementId) as Map<String, Any>
        //TODO we could probably use a little error handling on this cast.
    }

    suspend fun getAttribute(name: String): Any {
        return receiver.callJsFunctionWithResult(
            "return document.getElementById({}).getAttribute({})", elementId, ).toString()
    }

    suspend fun getInnerHtml(): String {
        return receiver.callJsFunctionWithResult(
            "return document.getElementById({}).innerHTML", elementId).toString()
    }

    suspend fun getText(): String {
        return receiver.callJsFunctionWithResult(
            "return document.getElementById({}).innerText", elementId).toString()
    }
}
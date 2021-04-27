package kweb.html

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
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
            "return document.getElementById({}).tagName", JsonPrimitive(elementId)).toString()
    }

    suspend fun getAttributes(): Map<String, JsonElement> {
        return receiver.callJsFunctionWithResult(
            "return document.getElementById({}).attributes", JsonPrimitive(elementId)) as Map<String, JsonElement>
        //TODO we could probably use a little error handling on this cast.
    }

    suspend fun getAttribute(name: String): Any {
        return receiver.callJsFunctionWithResult(
            "return document.getElementById({}).getAttribute({})", JsonPrimitive(elementId), ).toString()
    }

    suspend fun getInnerHtml(): String {
        return receiver.callJsFunctionWithResult(
            "return document.getElementById({}).innerHTML", JsonPrimitive(elementId)).toString()
    }

    suspend fun getText(): String {
        return receiver.callJsFunctionWithResult(
            "return document.getElementById({}).innerText", JsonPrimitive(elementId)).toString()
    }
}
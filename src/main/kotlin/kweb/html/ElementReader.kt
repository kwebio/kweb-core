package kweb.html

import kweb.Element
import kweb.WebBrowser
import kweb.util.KWebDSL
import kweb.util.escapeEcma
import java.util.concurrent.CompletableFuture

@KWebDSL
open class ElementReader(protected val receiver: WebBrowser, internal val elementId: String) {
    constructor(element: Element) : this(element.browser, element.id)

    init {
        require(receiver.kweb.isNotCatchingOutbound()) {
            """
            Reading the DOM when an outboundMessageCatcher is set is likely to have unintended consequences.
            Most likely you are trying to read the DOM within an `immediatelyOn {...}` block.
        """.trimIndent()
        }
    }

    val tagName: CompletableFuture<String> get() = receiver.callJsFunctionWithResult(
            "return document.getElementById(${elementId}).tagName").thenApply { it.toString() }
    val attributes: CompletableFuture<Map<String, Any>> get() = receiver.callJsFunctionWithResult(
            "return document.getElementById(${elementId}).attributes").thenApply { it as Map<String, Any> }
    fun attribute(name: String): CompletableFuture<Any> = receiver.callJsFunctionWithResult(
            "(return document.getElementById(${elementId}).getAttribute(\"${name.escapeEcma()}\"));")

    val class_ get() = attribute("class")
    val classes get() = class_.thenApply { it.toString().split(' ') }

    val innerHtml: CompletableFuture<String> get() = receiver.callJsFunctionWithResult(
            "(return document.getElementById(${elementId}).innerHTML);").thenApply { it.toString() }
    val text: CompletableFuture<String> = receiver.callJsFunctionWithResult(
            "(return document.getElementById(${elementId}).innerText);").thenApply { it.toString() }


}
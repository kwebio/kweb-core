package kweb.html

import kweb.Element
import kweb.KWebDSL
import kweb.WebBrowser
import kweb.escapeEcma
import java.util.concurrent.CompletableFuture

@KWebDSL
open class ElementReader(protected val receiver: WebBrowser, internal val jsExpression: String) {
    constructor(element: Element) : this(element.browser, element.jsExpression)

    init {
        require(receiver.kweb.isNotCatchingOutbound()) {
            """
            Reading the DOM when an outboundMessageCatcher is set is likely to have unintended consequences.
            Most likely you are trying to read the DOM within an `immediatelyOn {...}` block.
        """.trimIndent()
        }
    }

    val tagName: CompletableFuture<String> get() = receiver.evaluate("$jsExpression.tagName").thenApply { it.toString() }
    val attributes: CompletableFuture<Map<String, Any>> get() = receiver.evaluate("$jsExpression.attributes").thenApply { it as Map<String, Any> }
    fun attribute(name: String): CompletableFuture<Any> = receiver.evaluate("($jsExpression.getAttribute(\"${name.escapeEcma()}\"));")

    val class_ get() = attribute("class")
    val classes get() = class_.thenApply { it.toString().split(' ') }

    val innerHtml: CompletableFuture<String> get() = receiver.evaluate("($jsExpression.innerHTML);").thenApply { it.toString() }
    val text: CompletableFuture<String> = receiver.evaluate("($jsExpression.innerText);").thenApply { it.toString() }


}
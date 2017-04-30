package io.kweb.dom.element.read

import com.github.salomonbrys.kotson.fromJson
import io.kweb.WebBrowser
import io.kweb.dom.element.Element
import io.kweb.dom.element.KWebDSL
import io.kweb.escapeEcma
import io.kweb.gson
import java.util.*
import java.util.concurrent.CompletableFuture

@KWebDSL
open class ElementReader(protected val receiver: WebBrowser, internal val jsExpression: String) {
    constructor(element : Element) : this(element.webBrowser, element.jsExpression)

    val tagName: CompletableFuture<String> get() = receiver.evaluate("$jsExpression.tagName")
    val attributes: CompletableFuture<Map<String, String>> get() = receiver.evaluate("$jsExpression.attributes").thenApply { gson.fromJson<Map<String, String>>(it) }
    fun attribute(name: String): CompletableFuture<String> = receiver.evaluate("($jsExpression.getAttribute(\"${name.escapeEcma()}\"));")

    val children: CompletableFuture<ArrayList<Element>> get() {
        return receiver.evaluate("$jsExpression.children.length").thenApply({ numChildrenR ->
            val numChildren = numChildrenR.toInt()
            val childList = ArrayList<Element>()
            for (ix in 0..numChildren) {
                childList.add(Element(receiver, null, "$jsExpression.children[$ix]"))
            }
            childList
        })
    }

    val class_ get() = attribute("class")
    val classes get() = class_.thenApply { it.split(' ') }

    val innerHtml: CompletableFuture<String> get() = receiver.evaluate("($jsExpression.innerHTML);")
    val text: CompletableFuture<String> = receiver.evaluate("($jsExpression.innerText);")


}
package com.github.sanity.kweb.dom.element.read

import com.github.salomonbrys.kotson.fromJson
import com.github.sanity.kweb.RootReceiver
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.KWebDSL
import com.github.sanity.kweb.escapeEcma
import com.github.sanity.kweb.gson
import java.util.*
import java.util.concurrent.CompletableFuture

@KWebDSL
class ElementReader(private val receiver: RootReceiver, private val jsExpression: String) {
    constructor(element : Element) : this(element.rootReceiver, element.jsExpression)

    val tagName: CompletableFuture<String> get() = receiver.evaluate("$jsExpression.tagName")
    val attributes: CompletableFuture<Map<String, String>> get() = receiver.evaluate("$jsExpression.attributes").thenApply { gson.fromJson<Map<String, String>>(it) }
    fun attribute(name: String): CompletableFuture<String> = receiver.evaluate("($jsExpression.getAttribute(\"${name.escapeEcma()}\"));")

    val children: CompletableFuture<ArrayList<Element>> get() {
        return receiver.evaluate("$jsExpression.children.length").thenApply({ numChildrenR ->
            val numChildren = numChildrenR.toInt()
            val childList = ArrayList<Element>()
            for (ix in 0..numChildren) {
                childList.add(Element(receiver, "$jsExpression.children[$ix]"))
            }
            childList
        })
    }

    fun class_() = attribute("class")
    fun classList() = class_().thenApply { it.split(' ') }

    fun innerHtml(): CompletableFuture<String> = receiver.evaluate("($jsExpression.innerHTML);")
    fun text(): CompletableFuture<String> = receiver.evaluate("($jsExpression.innerText);")


}
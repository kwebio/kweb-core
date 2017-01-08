package com.github.sanity.kweb.dom

import com.github.salomonbrys.kotson.fromJson
import com.github.sanity.kweb.clientConduits.CoreReceiver
import com.github.sanity.kweb.escapeEcma
import com.github.sanity.kweb.gson
import com.github.sanity.kweb.quote
import com.github.sanity.kweb.random
import java.util.*
import java.util.concurrent.CompletableFuture

open class Element(val receiver: CoreReceiver, val jsExpression: String) {

    private fun <O> evaluate(js: String, outputMapper: (String) -> O): CompletableFuture<O>? {
        return receiver.evaluate(js).thenApply(outputMapper)
    }

    fun setAttribute(name: String, value: Any): Element {
        receiver.execute("$jsExpression.setAttribute(\"${name.escapeEcma()}\", ${if (value is String) value.escapeEcma().quote() else value});")
        return this
    }

    fun setInnerHTML(value: String): Element {
        receiver.execute(" $jsExpression.innerHTML=\"${value.escapeEcma()}\";")
        return this
    }

    fun text(value: String): HTMLReceiver {
        receiver.execute("""
                {
                    var ntn=document.createTextNode("${value.escapeEcma()}");
                    $jsExpression.appendChild(ntn);
                }
        """)
        return HTMLReceiver(this)
    }

    fun createElement(tag: String, attributes: Map<String, String> = Collections.emptyMap()): Element {
        val id = attributes["id"] ?: Math.abs(random.nextInt()).toString()
        val javaScript = StringBuilder()
        with(javaScript) {
            appendln("{")
            appendln("var newEl = document.createElement(\"$tag\");")
            if (!attributes.containsKey("id")) {
                appendln("newEl.setAttribute(\"id\", \"$id\");")
            }
            for ((name, value) in attributes) {
                appendln("newEl.setAttribute(\"$name\", \"$value\");")
            }
            appendln("$jsExpression.appendChild(newEl);")
            appendln("}")
        }
        receiver.execute(javaScript.toString())
        return Element(receiver, "document.getElementById(\"$id\")")
    }

    fun addEventListener(eventName: String, rh: CoreReceiver.() -> Boolean): Element {
        val callbackId = Math.abs(random.nextInt())
        val js = jsExpression + """
            .addEventListener(${eventName.quote()}, function() {
                callbackWs($callbackId, false)
            });
        """
        receiver.executeWithCallback(js, callbackId) {
            rh.invoke(receiver)
        }
        return this
    }

    val read: ElementReader get() = ElementReader(receiver, jsExpression)

    /*
     * HTMLReceiver helpers
     *
     * NOTE: Beware the fact that receivers cascade up if they can't
     *       match something in the inner-most block
     */

    fun html(receiver: HTMLReceiver.() -> Unit) {
        receiver.invoke(HTMLReceiver(this))
    }

}

class ElementReader(private val receiver: CoreReceiver, private val jsExpression: String) {
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
    val innerHtml: CompletableFuture<String> get() = receiver.evaluate("($jsExpression.innerHTML);")

}
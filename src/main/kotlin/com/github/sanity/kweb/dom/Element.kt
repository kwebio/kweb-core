package com.github.sanity.kweb.dom

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.toJson
import com.github.sanity.kweb.clientConduits.CoreReceiver
import com.github.sanity.kweb.dom.Element.ButtonType.button
import com.github.sanity.kweb.escapeEcma
import com.github.sanity.kweb.gson
import com.github.sanity.kweb.random
import com.github.sanity.kweb.toJson
import java.util.*
import java.util.concurrent.CompletableFuture

open class Element(open val receiver: CoreReceiver, open val jsExpression: String) {

    private fun <O> evaluate(js: String, outputMapper: (String) -> O): CompletableFuture<O>? {
        return receiver.evaluate(js).thenApply(outputMapper)
    }

    // TODO: Explicit support for global attributes from http://www.w3schools.com/tags/ref_standardattributes.asp
    // TODO: These should probably be accessed via a field like element.attr[GlobalAttributes.hidden], possibly
    // TODO: using generics to ensure the correct return-type

    fun setAttribute(name: String, value: Any): Element {
        receiver.execute("$jsExpression.setAttribute(\"${name.escapeEcma()}\", ${value.toJson()});")
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

    fun createElement(tag: String, attributes: Map<String, Any> = Collections.emptyMap()): Element {
        val id = attributes["id"] ?: Math.abs(random.nextInt()).toString()
        val javaScript = StringBuilder()
        with(javaScript) {
            appendln("{")
            appendln("var newEl = document.createElement(\"$tag\");")
            if (!attributes.containsKey("id")) {
                appendln("newEl.setAttribute(\"id\", \"$id\");")
            }
            for ((name, value) in attributes) {
                appendln("newEl.setAttribute(\"$name\", ${value.toJson()});")
            }
            appendln("$jsExpression.appendChild(newEl);")
            appendln("}")
        }
        receiver.execute(javaScript.toString())
        return Element(receiver, "document.getElementById(\"$id\")")
    }

    fun delete() {
        receiver.execute("$jsExpression.parentNode.removeChild($jsExpression)")
    }

    fun addEventListener(eventName: String, rh: CoreReceiver.() -> Unit): Element {
        val callbackId = Math.abs(random.nextInt())
        val js = jsExpression + """
            .addEventListener(${eventName.toJson()}, function() {
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
     * HTML helpers
     *
     * NOTE: Beware the fact that receivers cascade up if they can't
     *       match something in the inner-most block
     */

    fun h1(text: String, attributes: Map<String, String> = Collections.emptyMap()): HTMLReceiver {
        return createElement("h1", attributes).text(text)
    }

    fun ul(attributes: Map<String, String> = Collections.emptyMap()): ULElement {
        val e = createElement("ul", attributes)
        return ULElement(HTMLReceiver(e))
    }

    class ULElement(parent: Element) : HTMLReceiver(parent) {
        fun li(attributes: Map<String, String> = Collections.emptyMap()) = createElement("li", attributes)
    }

    val on: ONReceiver get() = ONReceiver(this)

    fun input(type: InputType, name: String? = null, initialValue: String? = null, size: Int? = null): InputElement {
        val attributes = HashMap<String, Any>()
        attributes.put("type", type.name)
        if (name != null) attributes.put("name", name)
        if (initialValue != null) attributes.put("value", initialValue)
        if (size != null) attributes.put("size", size)
        return InputElement(createElement("input", attributes))
    }

    class InputElement(val element: Element) : Element(element.receiver, element.jsExpression) {
        fun getValue(): CompletableFuture<String> = element.evaluate("$jsExpression.value", { s: String -> s }) ?: throw RuntimeException("Not sure why .evaluate() would return null")
        fun setValue(newValue: String) = element.receiver.execute("$jsExpression.value=${newValue.toJson()}")
    }


    enum class InputType {
        button, checkbox, color, date, datetime, email, file, hidden, image, month, number, password, radio, range, reset, search, submit, tel, text, time, url, week
    }

    fun button(type : ButtonType = button, autofocus : Boolean? = null, disabled : Boolean? = null) : Element {
        val attributes = HashMap<String, Any>()
        attributes.put("type", type.name)
        if (autofocus != null) attributes.put("autofocus", autofocus)
        if (disabled != null) attributes.put("disabled", disabled)
        return createElement("button", attributes)
    }

    enum class ButtonType {
        button, reset, submit
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
package com.github.sanity.kweb.dom

import com.github.sanity.kweb.clientConduits.CoreReceiver
import java.util.*

open class HTMLReceiver(private val parent: Element) : Element(parent.receiver, parent.jsExpression) {

    fun h1(text: String, attributes: Map<String, String> = Collections.emptyMap()): HTMLReceiver {
        return createElement("h1", attributes).text(text)
    }

    fun ul(attributes: Map<String, String> = Collections.emptyMap()): ULElement {
        val e = parent.createElement("ul", attributes)
        return ULElement(HTMLReceiver(e))
    }

    class ULElement(parent: Element) : HTMLReceiver(parent) {
        fun li(attributes: Map<String, String> = Collections.emptyMap()) = createElement("li", attributes)
    }

    val on: ONReceiver get() = ONReceiver(this)

}
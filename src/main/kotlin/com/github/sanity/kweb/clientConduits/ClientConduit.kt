package com.github.sanity.kweb.clientConduits

import com.github.sanity.kweb.dom.Element

/**
 * Created by ian on 1/1/17.
 */

abstract class ClientConduit(open val rh: CoreReceiver.() -> Unit) {
    abstract fun execute(clientId: String, message: String)

    abstract fun evaluate(clientId: String, expression: String, handler: (String) -> Unit)

    abstract fun executeWithCallback(clientId: String, js: String, callbackId: Int, handler: (String) -> Unit)
}

class ReadableElement(val tag: String, val attributes: Map<String, Object>)

class Document(private val receiver: CoreReceiver) {
    fun getElementById(id: String) = Element(receiver, "document.getElementById(\"$id\")")

    val body = Element(receiver, "document.body")
}

package com.github.sanity.kweb

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.plugins.KWebPlugin

/**
 * Created by ian on 1/1/17.
 */

abstract class ClientConduit(open val createPage: RootReceiver.() -> Unit, internal val plugins: List<KWebPlugin>) {

    abstract fun execute(clientId: String, message: String)

    abstract fun evaluate(clientId: String, expression: String, handler: (String) -> Unit)

    abstract fun executeWithCallback(clientId: String, js: String, callbackId: Int, handler: (String) -> Unit)
}

class ReadableElement(val tag: String, val attributes: Map<String, Any>)

class Document(private val receiver: RootReceiver) {
    fun getElementById(id: String) = Element(receiver, "document.getElementById(\"$id\")")

    val body = Element(receiver, "document.body")
}

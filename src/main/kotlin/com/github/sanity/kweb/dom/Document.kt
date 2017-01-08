package com.github.sanity.kweb.dom

import com.github.sanity.kweb.clientConduits.CoreReceiver

class Document(private val receiver: CoreReceiver) {
    fun getElementById(id: String) = Element(receiver, "document.getElementById(\"$id\")")

    val body = Element(receiver, "document.body")
}
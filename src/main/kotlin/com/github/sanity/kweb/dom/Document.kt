package com.github.sanity.kweb.dom

import com.github.sanity.kweb.RootReceiver
import com.github.sanity.kweb.dom.cookies.CookieReceiver
import com.github.sanity.kweb.dom.element.Element

class Document(private val receiver: RootReceiver) {
    fun getElementById(id: String) = Element(receiver, "document.getElementById(\"$id\")")

    val cookie = CookieReceiver(receiver)

    val body = BodyElement(receiver)
}

class BodyElement(rootReceiver: RootReceiver, id: String? = null) : Element(rootReceiver, "document.body", "body", id)
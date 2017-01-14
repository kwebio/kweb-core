package com.github.sanity.kweb.dom

import com.github.sanity.kweb.RootReceiver
import com.github.sanity.kweb.dom.cookies.CookieReceiver
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.storage.StorageReceiver
import com.github.sanity.kweb.dom.storage.StorageType

class Document(private val receiver: RootReceiver) {
    fun getElementById(id: String) = Element(receiver, "document.getElementById(\"$id\")")

    val cookie = CookieReceiver(receiver)

    val localStorage = StorageReceiver(receiver, StorageType.local)

    val sessionStorage = StorageReceiver(receiver, StorageType.session)

    val body = Element(receiver, "document.body")
}


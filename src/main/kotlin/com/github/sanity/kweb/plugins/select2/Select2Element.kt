package com.github.sanity.kweb.plugins.select2

import com.github.sanity.kweb.dom.element.creation.SelectElement

class Select2Element(val wrapped : SelectElement) {
    val on : Select2OnReceiver get() = Select2OnReceiver(wrapped)
}
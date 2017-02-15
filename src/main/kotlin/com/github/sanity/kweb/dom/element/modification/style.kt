package com.github.sanity.kweb.dom.element.modification

import com.github.sanity.kweb.dom.element.Element

/**
 * Created by ian on 2/12/17.
 */

val Element.style get() = StyleReceiver(this)

class StyleReceiver(private val parent : Element) {
    fun setDisplay(value : DisplayValues) {
        parent.execute("${parent.jsExpression}.style.display=\"$value\"")
    }

    enum class DisplayValues{
        none, block
    }
}
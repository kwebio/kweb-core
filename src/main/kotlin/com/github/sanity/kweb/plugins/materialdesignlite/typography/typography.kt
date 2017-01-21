package com.github.sanity.kweb.plugins.materialdesignlite.typography

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.modification.addClasses
import com.github.sanity.kweb.plugins.materialdesignlite.materialDesignLite

/**
 * Created by ian on 1/21/17.
 */

fun Element.typography(style: TypographyStyles): Element {
    require(materialDesignLite::class)
    addClasses("mdl-typography--" + style.classSubstring)
    return this
}

enum class TypographyStyles(val classSubstring: String) {
    display4("display-4"),
    display3("display-3"),
    display2("display-2"),
    display1("display-1"),
    headline("headline"),
    title("title")
}
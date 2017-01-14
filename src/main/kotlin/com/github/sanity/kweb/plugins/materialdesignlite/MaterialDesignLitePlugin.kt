package com.github.sanity.kweb.plugins.materialdesignlite

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.*
import com.github.sanity.kweb.dom.element.modification.addClasses
import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.plugins.materialdesignlite.MaterialDesignLitePlugin.MDLColor.*
import java.util.*

/**
 * Created by ian on 1/13/17.
 */
class MaterialDesignLitePlugin(private val colorScheme: ColorScheme = ColorScheme(blue, light_blue)) : KWebPlugin() {

    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        // From https://v4-alpha.getbootstrap.com/getting-started/download/#source-files
        startHead.appendln("""
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
<link rel="stylesheet" href="https://code.getmdl.io/1.3.0/material.${colorScheme.one}-${colorScheme.two}.min.css">
<script defer src="https://code.getmdl.io/1.3.0/material.min.js"></script>
<link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Roboto:300,400,500,700" type="text/css">
        """.trimIndent()
        )
    }

    override fun executeAfterPageCreation(): String {
        return "componentHandler.upgradeDom();"
    }

    data class ColorScheme(val one: MDLColor, val two: MDLColor) {
        init {
            if (one == two) {
                throw IllegalArgumentException("MDL does not permit colors one and two to be the same")
            }
            val specialColors = setOf(brown, blue_grey, grey)
            if ((one in specialColors) && (two in specialColors)) {
                throw IllegalArgumentException("MDL doesn't permit this color combination, see https://getmdl.io/customize/index.html")
            }
            if (one in specialColors && two !in specialColors) {
                throw IllegalArgumentException("MDL doesn't permit this color combination, see https://getmdl.io/customize/index.html")
            }
        }

    }

    enum class MDLColor {
        purple, deep_purple, indigo, blue, light_blue, cyan, teal, green, light_green, lime, yellow, amber, orange,
        brown, blue_grey, deep_orange, grey, red, pink
    }
}

// A convenience value
val materialDesignLite = com.github.sanity.kweb.plugins.materialdesignlite.MaterialDesignLitePlugin()

val Element.mdl get() = MDLElement(this)

class MDLElement(val element: Element) : Element(element) {

    fun drawerLayout(): MDLDrawerLayoutElement = MDLDrawerLayoutElement(element.div(attr.classes("mdl-layout", "mdl-js-layout", "mdl-layout--fixed-drawer")))

}

class MDLDrawerLayoutElement(val element: DIVElement) : DIVElement(element) {
    fun drawer(attributes: Map<String, Any> = HashMap()) = MDLDrawerElement(element.div(attributes.classes("mdl-layout__drawer")))

    fun content(attributes: Map<String, Any> = HashMap()) = element.main(attributes.classes("mdl-layout__content")).div(attr.classes("page-content"))
}

class MDLDrawerElement(val element: Element) : Element(element) {
    fun title(): Element = element.span(attr.classes("mdl-layout-title"))

    fun nav(): MDLNavElement = MDLNavElement(element.nav(attr.classes("mdl-navigation")))
}

class MDLNavElement(val element: Element) : Element(element) {
    fun link(href: String? = null, attributes: Map<String, Any> = Collections.emptyMap()) = element.a(href, attributes = attributes.classes("mdl-navigation__link"))
}

fun Element.typography(style: TypographyStyles): Element {
    addClasses("mdl-typography--" + style.classSubstring)
    return this
}

enum class TypographyStyles(val classSubstring: String) {
    display3("display-3")
}
package com.github.sanity.kweb.plugins.materialdesignlite

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.plugins.materialdesignlite.MaterialDesignLite.MDLColor.*

/**
 * Created by ian on 1/13/17.
 */
class MaterialDesignLite(private val colorScheme: ColorScheme = ColorScheme(blue, light_blue)) : KWebPlugin() {

    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        // From https://v4-alpha.getbootstrap.com/getting-started/download/#source-files
        startHead.appendln("""
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
<link rel="stylesheet" href="https://code.getmdl.io/1.3.0/material.${colorScheme.one}-${colorScheme.two}.min.css">
<script defer src="https://code.getmdl.io/1.3.0/material.min.js"></script>
        """.trimIndent()
        )
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
            if (one !in specialColors && two !in specialColors) {
                throw IllegalArgumentException("MDL doesn't permit this color combination, see https://getmdl.io/customize/index.html")
            }
        }

    }

    enum class MDLColor {
        purple, deep_purple, indigo, blue, light_blue, cyan, teal, green, light_green, lime, yellow, amber, orange, brown, blue_grey, deep_orange, grey, red, pink
    }
}

// A convenience value
val materialDesignLite = com.github.sanity.kweb.plugins.materialdesignlite.MaterialDesignLite()

val Element.mdl get() = MDLReceiver(this)

class MDLReceiver(val element: Element) {

}
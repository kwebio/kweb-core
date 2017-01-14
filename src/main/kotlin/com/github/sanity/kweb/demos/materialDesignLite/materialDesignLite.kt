package com.github.sanity.kweb.demos.bootstrap4

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.element.creation.h1
import com.github.sanity.kweb.dom.element.modification.setText
import com.github.sanity.kweb.plugins.materialdesignlite.TypographyStyles.display3
import com.github.sanity.kweb.plugins.materialdesignlite.materialDesignLite
import com.github.sanity.kweb.plugins.materialdesignlite.mdl
import com.github.sanity.kweb.plugins.materialdesignlite.typography

fun main(args: Array<String>) {
    println("Visit http://127.0.0.1:8091/ in your browser...")
    KWeb(port = 8091, plugins = listOf(materialDesignLite)) {
        doc.body.apply {
            mdl.drawerLayout().apply {
                drawer().apply {
                    title().setText("KWeb")
                    nav().apply {
                        link().setText("One")
                        link().setText("Two")
                        link().setText("Three")
                    }
                }
                content().apply {
                    h1().typography(display3).setText("This is the page content")
                }
            }
        }
    }
}
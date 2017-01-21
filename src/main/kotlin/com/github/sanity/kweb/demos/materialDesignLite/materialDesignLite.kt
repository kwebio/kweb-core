package com.github.sanity.kweb.demos.materialDesignLite

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.element.creation.InputType.text
import com.github.sanity.kweb.dom.element.modification.addText
import com.github.sanity.kweb.plugins.materialdesignlite.layout.drawerLayout
import com.github.sanity.kweb.plugins.materialdesignlite.materialDesignLite
import com.github.sanity.kweb.plugins.materialdesignlite.mdl
import com.github.sanity.kweb.plugins.materialdesignlite.textFields.textField

fun main(args: Array<String>) {
    println("Visit http://127.0.0.1:8091/ in your browser...")
    KWeb(port = 8091, plugins = listOf(materialDesignLite)) {
        doc.body.apply {
            mdl.drawerLayout().apply {
                drawer().apply {
                    title().addText("KWeb")
                    nav().apply {
                        link().addText("One")
                        link().addText("Two")
                        link().addText("Three")
                    }
                }
                content().apply {
                    mdl.textField(floatingLabel = true).apply {
                        val nameInput = input(text)
                        label(forInput = nameInput).addText("Name")
                    }
                    mdl.textField().apply {
                        val phoneInput = input(text, pattern = "[0-9]*")
                        label(forInput = phoneInput).addText("Phone")
                        error().addText("Digits only")
                    }
                }
            }
        }
    }
}
package com.github.sanity.kweb.demos.plugins.jquery

import com.github.sanity.kweb.Kweb
import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.creation.h1
import com.github.sanity.kweb.dom.element.modification.addText
import com.github.sanity.kweb.plugins.jqueryCore.jquery
import com.github.sanity.kweb.plugins.jqueryCore.jqueryCore


/**
 * Created by ian on 1/9/17.
 */

fun main(args: Array<String>) {
    Kweb(port = 8091, plugins = listOf(jqueryCore)) {
        doc.body.apply {
            h1(attr.classes("test")).addText("Simple Demo of JQuery plugin").apply {
                jquery(".test").apply {
                    on.click {
                        println("Clicked!")
                    }
                    on.mouseenter {
                        println("Mouse enter!")
                    }
                    on.mouseleave {
                        println("Mouse leave!")
                    }
                }
            }
        }
    }
}
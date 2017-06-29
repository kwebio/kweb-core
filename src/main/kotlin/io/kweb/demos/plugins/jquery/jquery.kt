package io.kweb.demos.plugins.jquery

import io.kweb.Kweb
import io.kweb.dom.attributes.attr
import io.kweb.dom.attributes.classes
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new
import io.kweb.plugins.jqueryCore.jquery
import io.kweb.plugins.jqueryCore.jqueryCore


/**
 * Created by ian on 1/9/17.
 */

fun main(args: Array<String>) {
    Kweb(port = 8091, plugins = listOf(jqueryCore)) {
        doc.body.new {
            h1(attributes = attr.classes("test")).addText("Simple Demo of JQuery plugin").apply {
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
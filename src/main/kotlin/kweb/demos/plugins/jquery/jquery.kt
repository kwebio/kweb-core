package kweb.demos.plugins.jquery

import kweb.Kweb
import kweb.dom.attributes.attr
import kweb.dom.attributes.classes
import kweb.dom.element.creation.tags.h1
import kweb.dom.element.new
import kweb.plugins.jqueryCore.jquery
import kweb.plugins.jqueryCore.jqueryCore


/**
 * Created by ian on 1/9/17.
 */

fun main(args: Array<String>) {
    Kweb(port = 8091, plugins = listOf(jqueryCore), buildPage = {
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
    })
}
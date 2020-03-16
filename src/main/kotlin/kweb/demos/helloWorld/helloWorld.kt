package kweb.demos.helloWorld

import kweb.Kweb
import kweb.dom.element.creation.tags.h1
import kweb.dom.element.new

fun main() {
    Kweb(port = 16097, buildPage = {
        doc.body.new {
            h1().text("Hello World!")
        }
    })
}
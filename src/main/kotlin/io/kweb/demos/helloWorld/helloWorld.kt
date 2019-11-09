package io.kweb.demos.helloWorld

import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new

fun main() {
    Kweb(port = 16097, buildPage = {
        doc.body.new {
            h1().text("Hello World!")
        }
    })
}
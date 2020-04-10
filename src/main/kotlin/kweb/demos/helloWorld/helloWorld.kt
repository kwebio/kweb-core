package kweb.demos.helloWorld

import kweb.Kweb
import kweb.h1
import kweb.new

fun main() {
    Kweb(port = 16097, buildPage = {
        doc.body.new {
            h1().text("Hello World!")
        }
    })
}
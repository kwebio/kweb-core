package kweb.demos.helloWorld

import kweb.*

fun main() {
    Kweb(port = 16097, buildPage = {
        doc.body.new {
            h1().text("Hello World!")
        }
    })
}
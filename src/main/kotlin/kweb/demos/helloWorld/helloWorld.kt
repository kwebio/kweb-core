package kweb.demos.helloWorld

import kweb.*

fun helloWorld1() {
    Kweb(port = 16097, buildPage = {
        doc.body.new {
            h1().text("Hello World!")
        }
    })
}

fun helloWorld2() {
    Kweb(port = 16097) {
        doc.body.new {
            ul().new {
                for (x in 1..5) {
                    li().text("Hello World $x!")
                }
            }
        }
    }
}
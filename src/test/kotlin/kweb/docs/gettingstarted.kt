package kweb.docs

// ANCHOR: hello_world
import kweb.*

fun main() {
    Kweb(port = 16097) {
        doc.body {
            h1().text("Hello World!")
        }
    }
}
// ANCHOR_END: hello_world

// ANCHOR: hello_world_2
fun helloWorld2() {
    Kweb(port = 16097) {
        doc.body {
            ul {
                for (x in 1..5) {
                    li().text("Hello World $x!")
                }
            }
        }
    }
}
// ANCHOR_END: hello_world_2

// ANCHOR: hello_world_3
fun helloWorld3() {
    Kweb(port = 16097) {
        doc.body {
            ul {
                // We can modify the UL element we just created in an element {} block
                element {
                    // Here we add a CSS class to the UL element
                    classes("list")
                }
                // We can define functions as an extension to ElementCreator allowing us to use it
                // within the DSL
                fun ElementCreator<ULElement>.createMessage(x: Int) {
                    li().text("Hello World $x!")
                }

                for (x in 1..5) {
                    createMessage(x)
                }
            }
        }
    }
}
// ANCHOR_END: hello_world_3


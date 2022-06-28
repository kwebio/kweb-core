package kweb.demos.testPages

import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.state.KVar
import kweb.state.render
import mu.KotlinLogging


fun main() {
    DiffTestPage()
}

class DiffTestPage {

    private val logger = KotlinLogging.logger {}

    val plugins = listOf(fomanticUIPlugin)
    val server: Kweb

    var username = KVar("Initial")
    init {

        /** Create a Kweb instance, and configure it to use the Fomantic
         * UI framework. Build a simple to-do list app listening on
         * http://localhost:7659/
         * */
        server = Kweb(port = 7659, debug = true, plugins = plugins, buildPage = {

            doc.head {
                // Not required, but recommended by HTML spec
                meta(name = "Description", content = "A page to test server updating")
            }

            doc.body {
                /** Kweb allows you to modularize your code however suits your needs
                best.  Here I use an extension function defined elsewhere to
                draw some util outer page DOM elements */
                /** Kweb allows you to modularize your code however suits your needs
                best.  Here I use an extension function defined elsewhere to
                draw some util outer page DOM elements */
                div(fomantic.ui.text.center.aligned.container) {
                    val usernameInput = input(initialValue = "Initial")
                    username = usernameInput.value
                    println("initialValue: ${usernameInput.value.value}")
                    usernameInput.setAttribute("class", "fomantic ui input")

                    render(username) {
                        p().text("username: $it")
                    }
                }

            }
        })
    }
}
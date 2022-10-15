package kweb.docs

import kweb.*
import kweb.InputType.text
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin

fun main(args: Array<String>) {
    Kweb(port = 16097, plugins = listOf(fomanticUIPlugin)) {
        doc.body {
            div {
                element {
                    classes("ui", "segment")
                }
                h1().text("Enter Your Name")
                val nameInput = input(type = text)
                br()
                span().text(nameInput.value.map { "Hello, $it" })
            }
        }
    }
}
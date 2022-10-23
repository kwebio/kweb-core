package kweb.docs

import kweb.*
import kweb.InputType.text
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.state.KVar

fun main() {
    Kweb(port = 16097, plugins = listOf(fomanticUIPlugin)) {
        doc.body {
            val name = KVar("")
             div(fomantic.ui.segment) {
                h1().text("Enter Your Name")
                input(type = text).value = name
            }
            div(fomantic.ui.segment) {
                span().text(name.map { "Hello, $it" })
            }
        }
    }
}
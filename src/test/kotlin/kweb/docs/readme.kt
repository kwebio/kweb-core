package kweb.docs

import kweb.*
import kweb.InputType.text
import kweb.state.KVar


fun main() {
    Kweb(port = 16097) {
        doc.body {
            val name = KVar("")
            div {
                h1().text("Enter Your Name")
                input(type = text).value = name
            }
            div {
                span().text(name.map { "Hello, $it" })
            }
        }
    }
}
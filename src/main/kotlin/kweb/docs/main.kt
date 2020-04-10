package kweb.docs

import kweb.*
import kweb.state.KVar

fun main(args: Array<String>) {
    val counter = KVar(0)

    Kweb(port = 8091, buildPage = {
        doc.body.new {
            p().text(counter.map {"You've clicked the button $it times"})
            button(type = ButtonType.button).text("Click me!").on.click {
                counter.value++
            }
        }
    })
}


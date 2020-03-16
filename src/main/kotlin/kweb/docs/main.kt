package kweb.docs

import kweb.Kweb
import kweb.dom.element.creation.tags.ButtonType.button
import kweb.dom.element.creation.tags.button
import kweb.dom.element.creation.tags.p
import kweb.dom.element.new
import kweb.state.KVar

fun main(args: Array<String>) {
    val counter = KVar(0)

    Kweb(port = 8091, buildPage = {
        doc.body.new {
            p().text(counter.map {"You've clicked the button $it times"})
            button(type = button).text("Click me!").on.click {
                counter.value++
            }
        }
    })
}


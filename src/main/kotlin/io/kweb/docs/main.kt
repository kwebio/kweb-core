package io.kweb.docs

import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.creation.tags.ButtonType.button
import io.kweb.dom.element.events.on
import io.kweb.dom.element.new
import io.kweb.state.KVar

fun main(args: Array<String>) {
    val counter = KVar(0)

    Kweb(port = 8091) {
        doc.body.new {
            p().text(counter.map {"You've clicked the button $it times"})
            button(type = button).text("Click me!").on.click {
                counter.value++
            }
        }
    }
}


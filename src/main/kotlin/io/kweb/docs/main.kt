package io.kweb.docs

import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.events.on
import io.kweb.dom.element.new
import io.kweb.state.KVar

fun main(args: Array<String>) {
    val counter = KVar(0)
    Kweb(port = 31337) {
        doc.body.new {
            h1().text("Welcome to Kweb").on.click {
                counter.value++
            }
            p().text(counter.map { "Visits: $it" })
        }
    }
}


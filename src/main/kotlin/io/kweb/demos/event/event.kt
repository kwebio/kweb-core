package io.kweb.demos.event

import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.input
import io.kweb.dom.element.events.on
import io.kweb.dom.element.new

/**
 * Created by ian on 2/21/17.
 */

fun main(args: Array<String>) {
    Kweb(4682) {
        doc.body.new {
            input().on.keydown { e ->
                println("Received: '${e}'")
            }
        }
    }
}
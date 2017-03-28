package com.github.sanity.kweb.demos.event

import com.github.sanity.kweb.Kweb
import com.github.sanity.kweb.dom.element.creation.tags.input
import com.github.sanity.kweb.dom.element.events.on
import com.github.sanity.kweb.dom.element.new

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
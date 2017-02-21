package com.github.sanity.kweb.demos.event

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.element.creation.input
import com.github.sanity.kweb.dom.element.events.on

/**
 * Created by ian on 2/21/17.
 */

fun main(args: Array<String>) {
    KWeb(4682) {
        doc.body.apply {
            input().on.keydown { e ->
                println("Received: '${e}'")
            }
        }
    }
}
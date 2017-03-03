package com.github.sanity.kweb.samples

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.element.creation.h1
import com.github.sanity.kweb.dom.element.events.on
import com.github.sanity.kweb.dom.element.modification.setText

/**
 * Created by ian on 3/3/17.
 */

// [START HW]
fun main(args: Array<String>) {
    KWeb(port = 7823) {
        var counter = 0
        val h1 = doc.body.h1()
        h1.setText("Hello World!")

        h1.on.click {
            h1.setText(counter.toString())
            counter++
        }
    }
}
// [END HW]
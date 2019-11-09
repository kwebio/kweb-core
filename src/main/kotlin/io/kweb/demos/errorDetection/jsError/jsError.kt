package io.kweb.demos.errorDetection.jsError

import io.kweb.Kweb

/**
 * Created by ian on 3/28/17.
 */

fun main(args: Array<String>) {
    Kweb(port = 4124, debug = true, buildPage = {
        doc.body.execute("deliberate message;")
    })
}
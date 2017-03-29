package com.github.sanity.kweb.demos.errorDetection.jsError

import com.github.sanity.kweb.Kweb

/**
 * Created by ian on 3/28/17.
 */

fun main(args: Array<String>) {
    Kweb(port = 4124, debug = true) {
        doc.body.execute("deliberate error;")
    }
}
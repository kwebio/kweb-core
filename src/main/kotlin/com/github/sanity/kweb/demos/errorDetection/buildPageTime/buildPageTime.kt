package com.github.sanity.kweb.demos.errorDetection.buildPageTime

import com.github.sanity.kweb.Kweb

/**
 * Created by ian on 3/10/17.
 */

fun main(args: Array<String>) {
    Kweb(port = 1234, debug = true) {
        Thread.sleep(1000)
    }
    Thread.sleep(2000)
}
package com.github.sanity.kweb.demos.jquery

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.plugins.jqueryCore.jquery
import com.github.sanity.kweb.plugins.jqueryCore.jqueryCore


/**
 * Created by ian on 1/9/17.
 */

fun main(args: Array<String>) {
    KWeb(8091, plugins = listOf(jqueryCore)) {
        doc.body.apply {
            h1("Simple Demo of JQuery plugin")
            Thread.sleep(5000)
            jquery("h1").remove()
        }
    }
}
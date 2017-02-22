package com.github.sanity.kweb.demos.plugins.select2

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.element.creation.select
import com.github.sanity.kweb.plugins.select2.Result
import com.github.sanity.kweb.plugins.select2.Suggestions
import com.github.sanity.kweb.plugins.select2.select2

/**
 * Created by ian on 2/22/17.
 */

fun main(args: Array<String>) {
    KWeb(5461, plugins = listOf(select2)) {
        doc.body.apply {
            val select = select()
            select.select2(suggestions = { Suggestions(results = listOf(Result("1", "One"), Result("2", "Two"))) }).on.select {
                println("Event: $it")
            }
        }
    }
}
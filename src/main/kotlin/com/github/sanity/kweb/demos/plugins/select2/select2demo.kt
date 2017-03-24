package com.github.sanity.kweb.demos.plugins.select2

import com.github.sanity.kweb.Kweb
import com.github.sanity.kweb.dom.element.creation.insert
import com.github.sanity.kweb.plugins.select2.Item
import com.github.sanity.kweb.plugins.select2.Suggestions
import com.github.sanity.kweb.plugins.select2.select2

/**
 * Created by ian on 2/22/17.
 */

fun main(args: Array<String>) {
    Kweb(5461, plugins = listOf(select2)) {
        doc.body.insert().apply {
            val select = select()
            select.select2(suggestions = { Suggestions(results = listOf(Item("1", "One"), Item("2", "Two"))) }).on.select {
                println("Event: $it")
            }
        }
    }
}
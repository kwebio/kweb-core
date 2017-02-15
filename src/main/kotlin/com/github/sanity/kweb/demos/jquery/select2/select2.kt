package com.github.sanity.kweb.demos.jquery.select2

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.set
import com.github.sanity.kweb.dom.element.creation.select
import com.github.sanity.kweb.plugins.select2.Result
import com.github.sanity.kweb.plugins.select2.Suggestions
import com.github.sanity.kweb.plugins.select2.select2

/**
 * Created by ian on 2/15/17.
 */

fun main(args: Array<String>) {
    KWeb(port = 8013, plugins = listOf(select2)) {
        doc.body.apply {
            val select = select(attr.set("style", "width: 50%"))
            select
            select.select2(placeholder = "Add an investor", allowClear = true, suggestions = { qp ->
                Suggestions(results = listOf(Result("1", qp+"dog"), Result("2", qp+"cat")))
            })
        }
    }
}
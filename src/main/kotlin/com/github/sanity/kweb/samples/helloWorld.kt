package com.github.sanity.kweb.samples

import com.github.sanity.kweb.Kweb
import com.github.sanity.kweb.dom.element.creation.tags.h1
import com.github.sanity.kweb.dom.element.modification.text
import com.github.sanity.kweb.dom.element.new

/**
 * Created by ian on 3/3/17.
 */

fun main(args: Array<String>) {
    Kweb(port = 7823, refreshPageOnHotswap = true) {
        doc.body.new().h1().text("Hello World!")
    }
}
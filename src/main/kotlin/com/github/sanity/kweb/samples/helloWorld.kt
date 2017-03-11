package com.github.sanity.kweb.samples

import com.github.sanity.kweb.Kweb
import com.github.sanity.kweb.dom.element.creation.h1
import com.github.sanity.kweb.dom.element.modification.setText

/**
 * Created by ian on 3/3/17.
 */

fun main(args: Array<String>) {
    Kweb(port = 7823, refreshPageOnHotswap = true) {
        doc.body.h1().setText("Hello World!")
    }
}
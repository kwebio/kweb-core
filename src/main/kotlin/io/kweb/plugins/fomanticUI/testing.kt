package io.kweb.plugins.fomanticUI

import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.a
import io.kweb.dom.element.creation.tags.div
import io.kweb.dom.element.new

/**
 * Created by ian on 4/1/17.
 */


fun main(args: Array<String>) {
    Kweb(port = 3525, plugins = listOf(fomanticUIPlugin), buildPage = {
        doc.body.new {
            div(fomantic.ui.container).new {
                div(fomantic.ui.three.item.menu).new {
                    a(attributes = fomantic.active.item).text("Editorials")
                    a(attributes = fomantic.item).text("Reviews")
                    a(attributes = fomantic.item).text("Upcoming Events")
                }
            }
        }
    })
}
package io.kweb.plugins.semanticUI

import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.a
import io.kweb.dom.element.creation.tags.div
import io.kweb.dom.element.modification.text
import io.kweb.dom.element.new

/**
 * Created by ian on 4/1/17.
 */


fun main(args: Array<String>) {
    Kweb(port = 3525, plugins = listOf(semanticUIPlugin)) {
        doc.body.new {
            div(sUI.ui.container).new {
                div(sUI.ui.three.item.menu).new {
                    a(attributes = sUI.active.item).text("Editorials")
                    a(attributes = sUI.item).text("Reviews")
                    a(attributes = sUI.item).text("Upcoming Events")
                }
            }
        }
    }
}
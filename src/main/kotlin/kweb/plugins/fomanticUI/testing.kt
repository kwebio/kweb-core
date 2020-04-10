package kweb.plugins.fomanticUI

import kweb.*

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
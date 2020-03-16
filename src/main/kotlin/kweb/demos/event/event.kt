package kweb.demos.event

import kweb.Kweb
import kweb.dom.element.creation.tags.input
import kweb.dom.element.new

/**
 * Created by ian on 2/21/17.
 */

fun main() {
    Kweb(4682, buildPage = {
        doc.body.new {
            input().on.keydown { e ->
                println("Received: '${e}'")
            }
        }
    })
}
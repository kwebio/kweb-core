package kweb.docs

// ANCHOR: plugin
import kweb.*
import kweb.plugins.fomanticUI.*

fun main() {
    Kweb(port = 16097, plugins = listOf(fomanticUIPlugin)) {
        // ...
    }
}
// ANCHOR_END: plugin

fun main2() {
    // ANCHOR: search
Kweb(port = 16097, plugins = listOf(fomanticUIPlugin)) {
    doc.body {
        div(fomantic.ui.icon.input).new {
            input(type = InputType.text, placeholder = "Search...")
            i(fomantic.search.icon)
        }
    }
}
    // ANCHOR_END: search
}
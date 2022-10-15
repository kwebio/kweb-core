package kweb.docs

import kweb.*

fun main() {
    Kweb(port = 16097) {
// ANCHOR: attach_1
doc.body {
    val label = h1()
    label.text("Click Me")
    label.on.click {
        label.text("Clicked!")
    }
}
// ANCHOR_END: attach_1

        // ANCHOR: read
doc.body {
    val input = input(type = InputType.text)
    input.on.keypress { keypressEvent ->
        println("Key Pressed: ${keypressEvent.key}")
    }
}
        // ANCHOR_END: read

        // ANCHOR: immediate
doc.body {
    val input = button(type = ButtonType.button)
    val label = span().text("Not clicked")
    input.onImmediate.click {
        label.text("Clicked!")
    }
}
        // ANCHOR_END: immediate
    }
}

package kweb.docs

import kotlinx.serialization.json.jsonPrimitive
import kweb.*

/*
 * NOTE: Indentation is weird in this file because it's used to generate the documentation, don't fix it!
 */

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


        doc.body {
            val inputButton = button(type = ButtonType.button)
            val label = span().text("Not clicked")
            // ANCHOR: retrieveJs
inputButton.on(retrieveJs = "(new Date()).getTime()").click { event ->
    label.text("Clicked at ${event.retrieved.jsonPrimitive.content}")
}
            // ANCHOR_END: retrieveJs
        }
        doc.body {
            // ANCHOR: retrieveJs2
val textInput = input(type = InputType.text)
val inputButton = button(type = ButtonType.button)
val label = span().text("Not clicked")
inputButton.on(retrieveJs = textInput.valueJsExpression).click { event ->
    label.text("Read textInput: ${event.retrieved.content}")
}
            // ANCHOR_END: retrieveJs2
        }
    }
}

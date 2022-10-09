package kweb.docs

import kotlinx.serialization.json.JsonPrimitive
import kweb.*

// ANCHOR: create
fun main() {
    Kweb(port = 16097) {
        doc.body {
            button().text("Click Me!")
        }
    }
}
// ANCHOR_END: create

fun foo() {
    Kweb(port = 16097) {
        doc.body {
            // ANCHOR: attr
val button = button()
button.text("Click Me!")
button.classes("bigbutton")
button.setAttribute("autofocus", true)
            // ANCHOR_END: attr

            // ANCHOR: delete
button.delete()
            // ANCHOR_END: delete

            // ANCHOR: attr2
button {
    attr {
        classes("bigbutton")
        setAttribute("autofocus", true)
    }
    text("Click Me!")
}
            // ANCHOR_END: attr2

            // ANCHOR: children
ul {
    li().text("One")
    li().text("Two")
}
            // ANCHOR_END: children

            // ANCHOR: children_new
val unorderedList : ULElement = ul()
unorderedList.new {
    li().text("One")
    li().text("Two")
}
            // ANCHOR_END: children_new

            // ANCHOR: read_value
val input: InputElement = input(type = InputType.text)
// A KVar is a mutable value to which you can add listeners
val inputKVar = input.value
inputKVar.addListener { old, new ->
    println("Input changed from $old to $new")
}
            // ANCHOR_END: read_value

            // ANCHOR: blink
val blink = element("blink").text("I am annoying!")
            // ANCHOR_END: blink
        }
    }
}


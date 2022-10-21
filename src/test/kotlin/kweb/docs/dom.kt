package kweb.docs

import kotlinx.serialization.json.JsonPrimitive
import kweb.*

/*
 * NOTE: Indentation is weird in this file because it's used to generate the documentation, don't fix it!
 */

// ANCHOR: create1
fun main() {
    Kweb(port = 16097) {
        doc.body {
            button().text("Click Me!")
        }
    }
}
// ANCHOR_END: create1

fun create2() {
// ANCHOR: create2
Kweb(port = 16097) {
    doc.body {
        table {
            tr {
                td().text("Name")
                td().text("Age")
            }
            tr {
                td().text("Alice")
                td().text("21")
            }
            tr {
                td().text("Bob")
                td().text("22")
            }
        }
    }
}
// ANCHOR_END: create2
}

fun foo() {
    Kweb(port = 16097) {
        doc.body {
            // ANCHOR: setattributes
val button = button()
button.text("Click Me!")
button.classes("bigbutton")
button["autofocus"] = true
            // ANCHOR_END: setattributes

            // ANCHOR: delete
button.delete()
            // ANCHOR_END: delete

            // ANCHOR: attr2
button {
    element {
        classes("bigbutton")
        this["autofocus"] = true
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
            blink.id // Remove unused warning
        }
    }
}


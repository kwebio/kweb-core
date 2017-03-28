package com.github.sanity.kweb.demos.todo

import com.github.sanity.kweb.Kweb
import com.github.sanity.kweb.dom.element.creation.ElementCreator
import com.github.sanity.kweb.dom.element.creation.tags.*
import com.github.sanity.kweb.dom.element.events.on
import com.github.sanity.kweb.dom.element.modification.addText
import com.github.sanity.kweb.dom.element.modification.delete
import com.github.sanity.kweb.dom.element.new
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future

fun main(args: Array<String>) {
    // Starts a web server listening on port 8091
    Kweb(8091, debug = true) {
        doc.body.new {
            // Add a header element to the body, along with some simple instructions.
            h1().addText("Simple Kweb demo - a to-do list")
            p().addText("Edit the text box below and click the button to add the item.  Click an item to remove it.")

            // If you're unfamiliar with the `apply` function, read this:
            //   http://beust.com/weblog/2015/10/30/exploring-the-kotlin-standard-library/

            // We element a <ul> element, and then use apply() to add things to it
            val ul = ul().new {

                // Add some initial items to the list
                for (text in listOf("one", "two", "three")) {
                    // We define this below
                    newListItem(text)
                }
            }

            // Next element an input element
            val inputElement = input(type = InputType.text, size = 20)

            // And a button to add a new item
            val button = button()
            button.addText("Add Item")
            // Here we register a callback, the code block will be called when the
            // user clicks this button.
            button.on.click {
                // This looks simple, but it is deceptively cool, and in more complex applications is the key to
                // hiding the client/server divide in a fairly efficient matter.  It uses Kotlin 1.1's new coroutines
                // functionality, see https://github.com/Kotlin/kotlinx.coroutines

                // We start an async block, which will allow us to use `await` within the block
                future {
                    // This is where async comes in.  inputElement.getValue() sends a message to the browser
                    // asking for the `value` of inputElement.  This will take time so
                    // inputElement.getValue() actually returns a future.  `await()` then uses coroutines
                    // to effectively wait until the future comes back, but crucially, without
                    // tying up a thread (which would getString very inefficient very quickly).
                    val newItemText = inputElement.getValue().await()

                    // And now we add the new item using our custom function
                    ul.new().newListItem(newItemText)

                    // And finally reset the value of the inputElement element.
                    inputElement.setValue("")
                }
            }
        }
    }
    Thread.sleep(10000)
}

// Here we use an extension method which can be used on any <UL> element to add a list item which will
// delete itself when clicked.
fun ElementCreator<ULElement>.newListItem(text: String) {
    li().apply {
        addText(text)
        on.click {
            delete()
        }
    }
}

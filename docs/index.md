---
layout: default
title: "Jekyll Docs Template"
---

1. TOC
{:toc}

#### What is KWeb?

KWeb is a library for building rich web applications in the [Kotlin](http://kotlinlang.org/)
programming language.

#### Features
* Build rich websites in the awesome Kotlin programming language
* Makes the barrier between web browser and web server largely invisible
* Avoid the complexities of the javascript ecosystem
* Seamlessly integrates with powerful tools like JQuery, MDL, and Bootstrap
* Works with [HotSwapAgent](http://hotswapagent.org/) to update your web browser instantly in 
  response to code changes

#### How does it work?
KWeb keeps all of the logic server-side, and uses websockets to communicate with web browsers.
We also take advantage of Kotlin's powerful new coroutines mechanism to efficiently handle
asynchronicity largely invisible to the programmer.

#### What does it look like?

Here we create a simple "todo" list app, note this is heavily commented, excluding comments it is
fewer than 40 lines of code.

```kotlin
import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.element.creation.*
import com.github.sanity.kweb.dom.element.events.on
import com.github.sanity.kweb.dom.element.modification.addText
import com.github.sanity.kweb.dom.element.modification.delete
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future

fun main(args: Array<String>) {
    // Starts a web server listening on port 8091
    KWeb(8091, debug = true) {
        doc.body.apply {
            // Add a header parent to the body, along with some simple instructions.
            h1().addText("Simple KWeb demo - a to-do list")
            p().addText("Edit the text box below and click the button to add the item.  Click an item to remove it.")

            // If you're unfamiliar with the `apply` function, read this:
            //   http://beust.com/weblog/2015/10/30/exploring-the-kotlin-standard-library/

            // We create a <ul> parent, and then use apply() to add things to it
            val ul = ul().apply {

                // Add some initial items to the list
                for (text in listOf("one", "two", "three")) {
                    // We define this below
                    newListItem(text)
                }
            }

            // Next create an input parent
            val inputElement = input(type = InputType.text, size = 20)

            // And a button to add a new item
            val button = button()
            button.addText("Add Item")
            // Here we register a callback, the code block will be called when the
            // user clicks this button.
            button.on.click {
                // This looks simple, but it is deceptively cool, and in more complex applications is 
                // the key to hiding the client/server divide in a fairly efficient matter.  It uses 
                // Kotlin 1.1's new coroutines functionality, see 
                // https://github.com/Kotlin/kotlinx.coroutines

                // We start an async block, which will allow us to use `await` within the block
                future {
                    // This is where async comes in.  inputElement.getValue() sends a message to the 
                    // browser asking for the `value` of inputElement.  This will take time so
                    // inputElement.getValue() actually returns a future.  `await()` then uses 
                    // coroutines to effectively wait until the future comes back, but crucially, 
                    // without tying up a thread (which would getString very inefficient very quickly).
                    val newItemText = inputElement.getValue().await()

                    // And now we add the new item using our custom function
                    ul.newListItem(newItemText)

                    // And finally reset the value of the inputElement parent.
                    inputElement.setValue("")
                }
            }
        }
    }
    Thread.sleep(10000)
}

// Here we use an extension method which can be used on any <UL> parent to add a list item which will
// delete itself when clicked.
fun ULElement.newListItem(text: String) {
    li().apply {
        addText(text)
        on.click { event ->
            delete() }
    }
}
```

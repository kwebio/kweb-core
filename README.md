## KWeb

### An experimental Kotlin framework for building rich interactive web applications in pure Kotlin

### Quick intro
The idea behind KWeb is to treat the web browser as a fairly dumb robot, keeping most of the intelligence server-side,
relaying instructions to the client via a websocket, and receiving responses from the client.  KWeb takes advantage
of the new coroutines functionality in the upcoming Kotlin 1.1 to handle asynchronously waiting for responses
from the client in a way that's almost transparent to the user of KWeb.

#### Status - 2017-01-08
KWeb is currently very experimental, at this stage more of a proof-of-concept and playground for ideas than anything
even close to being useful in production.

#### Getting started
1. Ensure you are using the "Early Access Preview 1.1" Kotlin plugin in IDEA, see "How to Try It" section at the 
bottom of JetBrain's [announcement](https://blog.jetbrains.com/kotlin/2016/12/kotlin-1-1-m04-is-here/).
2. Clone the repository and open as a Gradle project in IntelliJ IDEA: `git@github.com:sanity/kweb.git`

#### A simple example:

A super-minimalist to-do list.  Add items, and then remove them by clicking on them.

This is way longer than it needs to be due to the extensive comments, it may also be out-of-date, current version
[here](https://github.com/sanity/kweb/blob/master/src/main/kotlin/com/github/sanity/kweb/demos/todo/todo.kt).

```kotlin
import com.github.sanity.kweb.clientConduits.WebsocketsClientConduit
import com.github.sanity.kweb.dom.Element
import com.github.sanity.kweb.dom.Element.InputType.text
import kotlinx.coroutines.async
import kotlinx.coroutines.await

fun main(args: Array<String>) {
    WebsocketsClientConduit(8091) {
        // Starts a web server listening on port 8091
        doc.body.apply {
            // Add a header element to the body, along with some simple instructions.
            h1("Simple KWeb demo - a to-do list")
            p("Edit the text box below and click the button to add the item.  Click an item to remove it.")

            // If you're unfamiliar with the `apply` function, read this:
            //   http://beust.com/weblog/2015/10/30/exploring-the-kotlin-standard-library/

            // We create a <ul> element, and then use apply() to add things to it
            val ul = ul().apply {

                // Add some initial items to the list
                for (text in listOf("one", "two", "three")) {
                    // We define this below
                    newListItem(text)
                }
            }

            // Next create an input element
            val inputElement = input(type = text, size = 20)

            // And a button to add a new item
            button().text("Add Item")
                    // Here we register a callback, the code block will be called when the
                    // user clicks this button.
                    .on.click {

                // This looks simple, but it is deceptively cool, and in more complex applications is the key to
                // hiding the client/server divide in a fairly efficient matter.  It uses Kotlin 1.1's new coroutines
                // functionality, see https://github.com/Kotlin/kotlinx.coroutines

                // We start an async block, which will allow us to use `await` within the block
                async {
                    // This is where async comes in.  inputElement.getValue() sends a message to the browser
                    // asking for the `value` of inputElement.  This will take time so
                    // inputElement.getValue() actually returns a future.  `await()` then uses coroutines
                    // to effectively wait until the future comes back, but crucially, without
                    // tying up a thread (which would get very inefficient very quickly).
                    val newItemText = inputElement.getValue().await()

                    // And now we add the new item using our custom function
                    ul.newListItem(newItemText)

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
fun Element.ULElement.newListItem(text: String) {
    li().apply {
        text(text)
        on.click { delete() }
    }
}

```

##### Try it
1. Go to `src/main/kotlin/com/github/sanity/kweb/demos/todo/todo.kt` in IDEA
2. Run it
3. Visit [http://127.0.0.1:8091/](http://127.0.0.1:8091/) in your browser.

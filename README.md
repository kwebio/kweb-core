## KWeb

KWeb is an experimental [Kotlin](http://kotlinlang.org/) library for building rich interactive web applications in pure Kotlin in a way
that makes the distinction between web browser and server largely invisible to the programmer.

In particular, it can use the new [coroutines](https://github.com/Kotlin/kotlinx.coroutines) mechanism in 
upcoming Kotlin 1.1 to elegantly avoid [callback hell](http://callbackhell.com/).

It also incorporates a simple DSL for manipulating the browser's DOM.

### Why?
Building rich webapps normally requires navigating the
[awful Javascript ecosystem](https://hackernoon.com/how-it-feels-to-learn-javascript-in-2016-d3a717dd577f#.dvybqadhr),
choosing between a vast multitude of tools, transpilers, minifiers, state maintainers, and so on, most of which will
be obsolete in 6 months.  Then you're faced with handling interaction between client and server which is a whole other world of pain.

KWeb intends to solve this.

### How does it work?
KWeb treats the web browser as a fairly dumb robot, keeping most of the intelligence server-side,
relaying instructions to the client via a websocket, and receiving responses from the client.  Instructions to the server
are simple JavaScript snippets which the browser immediately executes, returning the result to the server if requested.

In this regard it is somewhat similar to [Vaadin](https://vaadin.com/home), although unlike Vaadin it doesn't incorporate
a large widget library, and it's much simpler to integrate into a project (just add the dependency).

There is also a similar mechanism in [Apache Wicket](https://wicket.apache.org/), although Wicket is mostly focussed on
transparently keeping server and client state in sync, motivated largely by the desire to have a graceful fallback
to HTML in the event that the browser doesn't support JavaScript.  This requirement is a lot less relevant today than it
was a decade ago.

Uniquely, KWeb takes advantage of the new coroutines functionality in the upcoming Kotlin 1.1 to handle 
asynchronously waiting for responses from the client in a way that's almost transparent to the user of KWeb, without tying up threads (a definite no-no if you plan on having more than a handful of people visiting your website at a time).

#### News
##### 2017-01-09: Plugins
KWeb now has a simple but flexible plugin mechanism, allowing the addition of support for 3rd party JavaScript libraries.

As an example, here is a [demo](https://github.com/sanity/kweb/blob/master/src/main/kotlin/com/github/sanity/kweb/demos/jquery/jquery.kt) of a rudimentary JQuery plugin being used, and [here](https://github.com/sanity/kweb/blob/master/src/main/kotlin/com/github/sanity/kweb/plugins/jqueryCore/JQueryCorePlugin.kt) you can see its implementation.  Currently it only supports .remove() - but you should get a feel for how easy it will be to flesh out other functionality.

##### 2017-01-08: Initial announcement
KWeb is currently very experimental, at this stage more of a proof-of-concept and playground for ideas than anything
even close to being useful in production.

The DOM DSL only covers a tiny subset of its eventual functionality, just barely more than is required for the TODO 
demo below.

#### Get involved
Have questions, ideas, or otherwise want to contribute?

Join our Slack channel [#kweb on kotlinlang](https://kotlinlang.slack.com/messages/kweb/)!  You may need to [sign up for kotlinlang](http://kotlinslackin.herokuapp.com/) first.

Take a look at our [open issues](https://github.com/sanity/kweb/issues), some of them should be quite easy to tackle, even
for someone new to KWeb.  I will do my best to review and merge any suitable pull requests promptly.

#### Getting started
1. Ensure you are using the "Early Access Preview 1.1" Kotlin plugin in IDEA, see "How to Try It" section at the 
bottom of JetBrain's [announcement](https://blog.jetbrains.com/kotlin/2016/12/kotlin-1-1-m04-is-here/).
2. Two options:
    * Clone the repository and open as a Gradle project in IntelliJ IDEA: `git@github.com:sanity/kweb.git`
    * Add as a dependency to your own Kotlin project, just be sure you're set up to use the
very latest Kotlin 1.1 release along with the plugin.  Dependency info here: [![](https://jitpack.io/v/sanity/kweb.svg)](https://jitpack.io/#sanity/kweb)

#### A simple example:

A super-minimalist to-do list.  Add items, and then remove them by clicking on them.

This is way longer than it needs to be due to the extensive comments, it may also be out-of-date, current version
[here](https://github.com/sanity/kweb/blob/master/src/main/kotlin/com/github/sanity/kweb/demos/todo/todo.kt).

```kotlin
import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.Element
import com.github.sanity.kweb.dom.Element.InputType.text
import kotlinx.coroutines.async
import kotlinx.coroutines.await

fun main(args: Array<String>) {
    // Starts a web server listening on port 8091
    KWeb(8091) {
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

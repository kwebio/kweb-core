---
layout: default
title: "Introduction to Kweb"
---

1. TOC
{:toc}

#### What is KWeb?

KWeb is a library for building rich web applications in the [Kotlin](http://kotlinlang.org/)
programming language.

#### Features
* Build websites in pure Kotlin
* Interact with the user just like powerful but complex JavaScript frameworks
  like React or Angular
* Makes the barrier between web-browser and web-server largely invisible
* Seamlessly integrates with powerful JavaScript libraries like JQuery, MDL, and Bootstrap
* Update your web browser instantly in response to code changes

#### How does it work?
KWeb keeps all of the logic server-side, and uses websockets to communicate with web browsers.
We also take advantage of Kotlin's powerful new coroutines mechanism to efficiently handle
asynchronicity largely invisible to the programmer.

#### What does it look like?

Here we create a simple "todo" list app:

```kotlin
fun main(args: Array<String>) {
    KWeb(8091, debug = true) {
        doc.body.apply {
            h1().addText("Simple KWeb demo - a to-do list")
            p().addText("Edit the text box below and click the button to add the item.  Click an item to remove it.")

            val ul = ul().apply {
                for (text in listOf("one", "two", "three")) {
                    newListItem(text)
                }
            }

            val inputElement = input(type = InputType.text, size = 20)

            val button = button()
            button.addText("Add Item")
            button.on.click {
                future {
                    val newItemText = inputElement.getValue().await()
                    ul.newListItem(newItemText)
                    inputElement.setValue("")
                }
            }
        }
    }
    Thread.sleep(10000)
}

fun ULElement.newListItem(text: String) {
    li().apply {
        addText(text)
        on.click { event ->
            delete() }
    }
}
```
**Next: [Setting Up]({{ site.baseurl }}{% post_url 2017-03-03-getting-started %}) >>>>**
---
title: Database
---

# Overview

[Shoebox](https://github.com/kwebio/shoebox) is a simple key-value store
that supports the [observer
pattern](https://en.wikipedia.org/wiki/Observer_pattern), and is a
sister project to Kweb. It supports a number of [back-end storage
engines](https://github.com/kwebio/shoebox/tree/master/src/main/kotlin/kweb/shoebox/stores)
including in-memory (userful for testing), and
[MapDB](https://mapdb.org/) (the best for production use).

Kweb doesn\'t require you to use Shoebox. You\'re free to use any
database, either directly, or via a database abstraction layer such as
[Exposed](https://github.com/jetbrains/Exposed). Kotlin has a [wide
selection](https://kotlin.link/?q=database#libraries/frameworks-database)
to choose from, as does
[Java](https://java-lang.github.io/awesome-java/#database).

The main benefit of using Shoebox is its integration with Kweb\'s [state
management](http://docs.kweb.io/en/latest/state.html).

We\'ll assume you\'ve taken a minute or two to review
[Shoebox](https://github.com/kwebio/shoebox) and get the general idea of
how it\'s used.

# Shoebox and State

This example shows how *toVar* can be used to convert a value in a
Shoebox to a [KVar](/en/latest/state.html):

``` kotlin
fun main() {
    data class User(val name : String, val email : String)
    val users = Shoebox<User>()
    users["aaa"] = User("Ian", "ian@ian.ian")

    Kweb(port = 16097) {
        doc.body {
            val user = toVar(users, "aaa")
            ul {
                li().text(user.map {"Name: ${it.name}"})
                li().text(user.map {"Email: ${it.email}"})

            }
        }
    }
}
```

# Future Development

In the future Shoebox will support back-end cloud services like [AWS
Pub/Sub Messaging](https://aws.amazon.com/pub-sub-messaging/) and
[Dynamo DB](https://aws.amazon.com/dynamodb/), which would enable
unlimited scalability. New storage backends can be added easily to
Shoebox by implementing the
[Store](https://github.com/kwebio/shoebox/blob/master/src/main/kotlin/kweb/shoebox/Store.kt)
interface.

# Working Example

For a more complete example of using Shoebox for persistent storage see
the [to do
demo](https://github.com/kwebio/kweb-demos/tree/master/todoList).

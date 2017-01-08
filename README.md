### KWeb, an experimental Kotlin framework for building rich interactive web applications in pure Kotlin

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

#### An introductory demo

A very simple implementation of the old-perennial: the to-do list.  Allows you to add items to a list, and remove
them by clicking on them.

This makes use of the very useful apply() function, if you're unfamiliar with it I recommend you read 
[Exploring the Kotlin Standard Library](http://beust.com/weblog/2015/10/30/exploring-the-kotlin-standard-library/).

Take a look at [the code](https://github.com/sanity/kweb/blob/master/src/main/kotlin/com/github/sanity/kweb/demos/todo/todo.kt).

##### Try it
1. Take a look at `src/main/kotlin/com/github/sanity/kweb/demos/todo/todo.kt` in IDEA
2. Run it
3. Visit [http://127.0.0.1:8091/](http://127.0.0.1:8091/) in your browser.

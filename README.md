# Kweb - A Kotlin web framework

<div align="center">
  <a href="https://github.com/kwebio/kweb-core/actions/workflows/build.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/kwebio/kweb-core/build.yml?branch=master&label=tests&style=flat-square&logo=github%20actions" alt="continuous integration status" >
  </a>
  <a href="https://matrix.to/#/#kweb:matrix.org">
    <img src="https://img.shields.io/badge/chat-matrix-blue?style=flat-square&logo=matrix&color=0B9CD6" alt="matrix" >
  </a>
  <a href="https://docs.kweb.io/book/gettingstarted.html">
    <img src="https://img.shields.io/maven-central/v/io.kweb/kweb-core?style=flat-square&logo=maven&label=kweb-core&color=374991" >
  <a href="https://github.com/kwebio/kweb-core/blob/master/LICENSE">
    <img src="https://img.shields.io/github/license/kwebio/kweb-core?style=flat-square&logo=gnu&color=3DA751" >
  </a>
  <a href="https://twitter.com/kwebio">
    <img alt="Twitter Follow" src="https://img.shields.io/twitter/follow/kwebio?logo=twitter&color=1DA1F2&style=flat-square&label=%40kwebio" >
  </a>
</div>

## Quick Start

Read the [Introduction](https://docs.kweb.io/book/intro.html) or 
[Getting Started](https://docs.kweb.io/book/gettingstarted.html) from 
the [Kweb User Manual](https://docs.kweb.io/book/).

## Why another web framework?

Kweb is designed to make it easy for developers to create modern websites without having to worry about the complexities of communication between the server and the browser. With a unified codebase, you can focus on creating an intuitive and user-friendly interface, rather than spending time on technical details. By streamlining the development process, Kweb makes it easier to build functional and beautiful websites that meet the needs of your users.

## How does it work?

Kweb is a remote interface for a web browser's DOM (Document Object Model). With Kweb, you can create and manipulate DOM elements, and listen for and handle events, all using an intuitive Kotlin DSL that mirrors the structure of the HTML being created. Kweb is built on the Ktor framework, which handles HTTP, HTTPS, and WebSocket transport, and is optimized to minimize latency and resource usage on both the server and browser.

## Example
  
```kotlin
import kweb.*
import kweb.InputType.text


fun main() {
    Kweb(port = 16097) {
        doc.body {
            val name = kvar("")
            div {
                h1().text("Enter Your Name")
                input(type = text).value = name
            }
            div {
                span().text(name.map { "Hello, $it" })
            }
        }
    }
}
```

This example illustrates [creating DOM elements](https://docs.kweb.io/book/dom.html#creating-dom-elements-and-fragments),
[modifying elements](https://docs.kweb.io/book/dom.html#adding-attributes), 
[KVars](https://docs.kweb.io/book/state.html#building-blocks), and binding 
[input elements](https://docs.kweb.io/book/dom.html#input-elements).

  <kbd>
<img src="https://user-images.githubusercontent.com/23075/197428328-9a42d5e2-f6c9-43f8-9d7c-62fe1a445dab.gif" />
  </kbd>
  
## Learn More

* [User Manual](http://docs.kweb.io/book)
* [API Documentation](https://docs.kweb.io/api)
* [Example Project](https://github.com/freenet/freenetorg-website/)
* [Questions, Feedback, Bugs](https://github.com/kwebio/kweb-core/issues)
* [Chat with us](https://matrix.to/#/#kweb:matrix.org)
* [Frequently Asked Questions](https://docs.kweb.io/book/faq.html)

## Thanks
  
YourKit supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.

YourKit is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/), [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/), and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).

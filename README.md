# Kweb - The Kotlin web framework for backend developers

<div align="center">
  <a href="https://github.com/kwebio/kweb-core/actions/workflows/build.yml">
    <img src="https://img.shields.io/github/workflow/status/kwebio/kweb-core/build?label=tests&style=flat-square&logo=github%20actions" alt="continuous integration status" />
  </a>
  <a href="https://matrix.to/#/#kweb:matrix.org">
    <img src="https://img.shields.io/badge/chat-matrix-blue?style=flat-square&logo=matrix&color=informational" alt="matrix" />
  </a>
  <a href="https://docs.kweb.io/book/gettingstarted.html">
    <img src="https://img.shields.io/maven-central/v/io.kweb/kweb-core?style=flat-square&logo=maven&color=blueviolet" />
  <a href="https://github.com/kwebio/kweb-core/blob/master/LICENSE">
    <img src="https://img.shields.io/github/license/kwebio/kweb-core?style=flat-square&logo=gnu&color=informational" \>
  </a>
</div>

## Quick Start

Read the [Introduction](https://docs.kweb.io/book/intro.html) or 
[Getting Started](https://docs.kweb.io/book/gettingstarted.html) from 
the [Kweb Book](https://docs.kweb.io/book/).

## Why another web framework?

Modern websites consist of at least two [tightly
coupled](https://en.wikipedia.org/wiki/Coupling_(computer_programming))
components, one runs in the browser, the other on the server. These are
often written in different programming languages and must communicate
with each other over an HTTP(S) connection.

Kweb's goal is to eliminate this server/browser separation so you can
focus on building your website or user interface, not on the plumbing.

## What is Kweb?
  
Kweb is a remote interface to the web browser's DOM, driven by the server. With Kweb, you can create and manipulate DOM elements, and even bind values in your realtime database to DOM elements so they update automatically when the database changes. 

Kweb is built on the [Ktor](https://ktor.io/) framework, which handles HTTP, HTTPS, and WebSocket transport. You don't need to know Ktor to use Kweb, but if you already have a Ktor app, you can [embed Kweb as a feature](https://github.com/kwebio/kweb-demos/blob/master/ktorFeature/src/FeatureApp.kt).

A common concern about server-driven interfaces is that they can feel sluggish. Kweb solves this problem by preloading instructions to the browser so that they are executed immediately on browser events, without the need for a round-trip to the server. 

## Example
  
```kotlin
import kweb.*
import kweb.InputType.text
import kweb.state.KVar


fun main() {
    Kweb(port = 16097) {
        doc.body {
            val name = KVar("")
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

### Result

This example illustrates [creating DOM elements](https://docs.kweb.io/book/dom.html#creating-dom-elements-and-fragments),
[modifying elements](https://docs.kweb.io/book/dom.html#adding-attributes), 
[KVars](https://docs.kweb.io/book/state.html#building-blocks), and binding 
[input elements](https://docs.kweb.io/book/dom.html#input-elements).

  <kbd>
<img src="https://user-images.githubusercontent.com/23075/197428328-9a42d5e2-f6c9-43f8-9d7c-62fe1a445dab.gif" />
  </kbd>
  
## Learn More

* [The Kweb Book](http://docs.kweb.io/book) (user manual)
* [API Documentation](https://docs.kweb.io/api)
* [Example Project](https://github.com/freenet/freenetorg-website/)
* [Questions, Feedback, Bugs](https://github.com/kwebio/kweb-core/issues)
* [Chat with us](https://matrix.to/#/#kweb:matrix.org)
* [Frequently Asked Questions](https://docs.kweb.io/book/faq.html)

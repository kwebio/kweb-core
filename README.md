# Kweb 🦆

<div align="center">
  <!-- Github Actions -->
  <a href="https://github.com/kwebio/kweb-core/actions/workflows/build.yml">
    <img src="https://img.shields.io/github/workflow/status/kwebio/kweb-core/build?label=tests&style=flat-square&logo=github%20actions" alt="continuous integration status" />
  </a>
  <a href="https://matrix.to/#/#kweb:matrix.org">
    <img src="https://img.shields.io/badge/chat-matrix-blue?style=flat-square&logo=matrix&color=informational" alt="matrix" />
  </a>
  <a href="https://docs.kweb.io/book/gettingstarted.html">
    <img src="https://img.shields.io/github/v/release/kwebio/kweb-core?label=dependency&sort=semver&style=flat-square&logo=gradle&color=blueviolet&include_prereleases" />
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

Kweb is a remote interface to the web browser's DOM. You can create and
manipulate DOM elements, and listen for and handle DOM events.

Kweb has a state system that lets you [easily](https://docs.kweb.io/book/state.html) 
bind values in your realtime database to DOM elements. This way, the 
elements are automatically updated when the database changes.

A common concern about this approach is that the user interface might feel
sluggish if it is server-driven. Kweb solves this problem by
[preloading](https://docs.kweb.io/en/latest/events.html#immediate-events)
instructions to the browser. This way, the instructions are executed
immediately on browser events without a server round-trip.

Kweb is built on the [Ktor](https://ktor.io/) framework, which handles
HTTP, HTTPS, and WebSocket transport. You don't need to know
Ktor to use Kweb, but if you've already got a Ktor app you can [embed
Kweb as a feature](https://github.com/kwebio/kweb-demos/blob/master/ktorFeature/src/FeatureApp.kt).

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

This demo illustrates [creating DOM elements](https://docs.kweb.io/book/dom.html#creating-dom-elements-and-fragments),
[modifying elements](https://docs.kweb.io/book/dom.html#adding-attributes), 
[KVars](https://docs.kweb.io/book/state.html#building-blocks), and binding 
[input elements](https://docs.kweb.io/book/dom.html#input-elements).

![video](readme-video.gif)

## Learn More

* [The Kweb Book](http://docs.kweb.io/book) (user manual)
* [API Documentation](https://docs.kweb.io/api)
* [Example Project](https://github.com/freenet/freenetorg-website/)
* [Questions, Feedback, Bugs](https://github.com/kwebio/kweb-core/issues)
* [Chat with us](https://matrix.to/#/#kweb:matrix.org)
* [Frequently Asked Questions](https://docs.kweb.io/book/faq.html)

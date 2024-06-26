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

## Notice of Project Status

Due to recent [changes](https://www.reddit.com/r/java/comments/1dkgh85/psa_maven_central_publishing_requires_token_now/) in Sonatype's deployment requirements, persistent [headaches](https://www.reddit.com/r/Kotlin/comments/y042g1/rant_gradle_is_an_embarrassment_to_the_javakotlin/) with JVM ecosystem tooling, and unresolved memory leak problems, I regret to inform you that I am no longer able to actively support Kweb. The last straw was Sonatype breaking the automatic deployment process by requiring a token for authentication, and [apparently](https://help.sonatype.com/en/user-tokens.html) requiring a paid upgrade to their Pro service to fix it. About 80% of my time over the past year spent on Kweb has been wrestling with tooling issues rather than improving the software. This, combined with the other challenges, has made it untenable for me to continue maintaining this framework.

If you are interested in taking over the project or contributing to its development, please feel free to fork the repository. If you need to contact me I'm @sanity on [X](https://x.com/sanity) and [Reddit](https://reddit.com/u/sanity).

## Quick Start

Read the [Introduction](https://docs.kweb.io/book/intro.html) or 
[Getting Started](https://docs.kweb.io/book/gettingstarted.html) from 
the [Kweb User Manual](https://docs.kweb.io/book/).

## Why another web framework?

Kweb is designed to make it easy for developers to create modern websites without having to worry about the complexities of communication between the server and the browser. With a unified codebase, you can focus on creating an intuitive and user-friendly interface, rather than spending time on technical details. By streamlining the development process, Kweb makes it easier to build functional and beautiful websites that meet the needs of your users.

## How does it work?

Kweb is a remote interface for a web browser's DOM (Document Object Model). With Kweb, you can create and manipulate DOM elements, and listen for and handle events, all using an intuitive Kotlin DSL that mirrors the structure of the HTML being created. Kweb is built on the Ktor framework, which handles HTTP, HTTPS, and WebSocket transport, and is optimized to minimize latency and resource usage on both the server and browser.

### Note on Memory Leak Issue

We have identified a memory leak issue that may affect users when using the `InputElement` class, we're working on a [fix](https://github.com/kwebio/kweb-core/pull/611) but can't make any promises if/when it will be completed. We recommend that you run a memory profiler to see if you're affected.

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

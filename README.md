## Kweb 🦆

[![](https://jitpack.io/v/kwebio/kweb-core.svg)](https://jitpack.io/#kwebio/kweb-core) [![Build Status](https://github.com/kwebio/kweb-core/workflows/tests/badge.svg?branch=master)](https://github.com/kwebio/kweb-core/actions?query=branch%3Amaster+workflow%3Atests)

Kweb is a new way to create beautiful, efficient, and scalable websites in Kotlin, quickly.

```kotlin
import kweb.*

fun main() {
  Kweb(port = 16097) {
    doc.body.new {
      h1().text("Hello World!")
    }
 }
}
```

Modern websites consist of at least two tightly coupled components, one runs in the browser, the other on the server. These are often written in different programming languages and must communicate with each other over an HTTP connection.

Kweb’s goal is to eliminate this server/browser separation so that your webapp’s architecture is determined by the problem you’re solving, rather than the limitations of today’s tools.

* [User Manual](http://docs.kweb.io/)
* [Live Example](http://demo.kweb.io:7659/)
* [Template Repo](https://github.com/kwebio/kweb-template)
* [Example Projects](https://github.com/kwebio/kweb-demos)
* [Support](https://github.com/kwebio/kweb-core/issues)
* [FAQ](http://docs.kweb.io/en/latest/faq.html)

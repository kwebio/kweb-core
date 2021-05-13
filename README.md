# Kweb 🦆

[![](https://jitpack.io/v/kwebio/kweb-core.svg)](https://jitpack.io/#kwebio/kweb-core) [![Build Status](https://github.com/kwebio/kweb-core/workflows/tests/badge.svg?branch=master)](https://github.com/kwebio/kweb-core/actions)

## Quick Start

Read [Getting Started](http://docs.kweb.io/en/latest/gettingstarted.html) from the [User Manual](http://docs.kweb.io/).

## Overview

Kweb is a new way to create beautiful, efficient, and scalable websites in [Kotlin](https://kotlinlang.org/), where server-browser communication is handled automatically.

## Example

```kotlin
import kweb.*

fun main() {
  Kweb(port = 16097) {
    doc.body {
      h1().text("Hello World!")
    }
 }
}
```

## Learn More

* [User Manual](http://docs.kweb.io/)
* [API Documentation](https://dokka.kweb.io/kweb-core/)
* [Live Example](http://demo.kweb.io:7659/)
* [Template Repo](https://github.com/kwebio/kweb-template)
* [Example Projects](https://github.com/kwebio/kweb-demos)
* [Questions, Feedback, Bugs](https://github.com/kwebio/kweb-core/issues)
* [Chat with us](https://gitter.im/kwebio/Lobby)
* [Frequently Asked Questions](http://docs.kweb.io/en/latest/faq.html)

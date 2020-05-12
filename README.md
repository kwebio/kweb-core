# Kweb 🦆

[![](https://jitpack.io/v/kwebio/kweb-core.svg)](https://jitpack.io/#kwebio/kweb-core) [![Build Status](https://github.com/kwebio/kweb-core/workflows/tests/badge.svg?branch=master)](https://github.com/kwebio/kweb-core/actions?query=branch%3Amaster+workflow%3Atests)

## Quick Start

Read [Getting Started](http://docs.kweb.io/en/latest/gettingstarted.html) from the [User Manual](http://docs.kweb.io/).

## Overview

Kweb is a new way to create beautiful, efficient, and scalable websites in Kotlin, where server-browser communication is handled automatically.

## Simplest Example

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

### Learn More...

* [User Manual](http://docs.kweb.io/)
* [Live Example](http://demo.kweb.io:7659/)
* [Template Repo](https://github.com/kwebio/kweb-template)
* [Example Projects](https://github.com/kwebio/kweb-demos)
* [Questions, Feedback, Bugs](https://github.com/kwebio/kweb-core/issues)
* [Frequently Asked Questions](http://docs.kweb.io/en/latest/faq.html)

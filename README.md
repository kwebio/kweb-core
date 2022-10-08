# Kweb 🦆

<div align="center">
  <!-- Github Actions -->
  <a href="https://github.com/kwebio/kweb-core/actions/workflows/build.yml">
    <img src="https://img.shields.io/github/workflow/status/kwebio/kweb-core/build?label=CI&style=flat-square" alt="continuous integration status" />
  </a>
  <a href="https://matrix.to/#/#kweb:matrix.org">
    <img src="https://img.shields.io/matrix/kweb:matrix.org?label=matrix&logo=matrix&style=flat-square&color=blue" alt="matrix" />
  </a>
  <a href="https://jitpack.io/#kwebio/kweb-core">
    <img src="https://img.shields.io/github/v/release/kwebio/kweb-core?label=latest&sort=semver&flat-square&color=bluevilot" />
  </a>
</div>

## Quick Start

Read [Getting Started](https://docs.kweb.io/book/gettingstarted.html) from the [Kweb Book](https://docs.kweb.io/book/).

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

## API Stability

Kweb's API is likely to change between now and our 1.0.0 release, so you may need to modify your code to stay current 
with Kweb versions. We won't do this without a good reason.

## Learn More

* [User Manual](http://docs.kweb.io/book)
* [API Documentation](https://docs.kweb/io/api)
* [Example Project](https://github.com/freenet/freenetorg-website/tree/staging/src/main/kotlin/org/freenet/website)
* [Template Repo](https://github.com/kwebio/kweb-template)
* [Examples](https://github.com/kwebio/kweb-demos)
* [Questions, Feedback, Bugs](https://github.com/kwebio/kweb-core/issues)
* [Chat with us](https://matrix.to/#/#kweb:matrix.org)
* [Frequently Asked Questions](https://docs.kweb.io/book/faq.html)

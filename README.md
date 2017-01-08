# KWeb

## Overview



## Features

## Usage

### Adding library dependency

You can use this library by adding a dependency for Gradle, Maven, SBT, Leiningen or another Maven-compatible dependency management system thanks to Jitpack:

[![](https://jitpack.io/v/sanity/kweb.svg)](https://jitpack.io/#sanity/kweb)

### Basic usage from Kotlin

```kotlin
import com.github.sanity.kweb.clientConduits.WebsocketsClientConduit
import kotlinx.coroutines.async
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    WebsocketsClientConduit(8091) {
        thread {
            async {
                var clicks = 0
                val heading = doc.body.appendChild("h1")
                heading.setInnerHTML("0 clicks")
                heading.addEventListener("click") {
                    clicks++
                    heading.setInnerHTML("$clicks clicks")
                    false
                }
            }
        }
        false
    }
    Thread.sleep(10000)
}

```

### License
Released under the [LGPL](https://en.wikipedia.org/wiki/GNU_Lesser_General_Public_License) version 3 by [Ian Clarke](http://blog.locut.us/) of [Stacks](http://trystacks.com/).

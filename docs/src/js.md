# JavaScript Interop

<!-- toc -->

## Introduction

Kweb's DOM interaction functionality is build on top of two functions that allow you to interact
directly with the browser's JavaScript interpreter:

* [WebBrowser.callJsFunction()](https://docs.kweb.io/api/kweb-core/kweb/-web-browser/call-js-function.html)
* [WebBrowser.callJsFunctionWithResult()](https://docs.kweb.io/api/kweb-core/kweb/-web-browser/call-js-function-with-result.html)

[Element](https://docs.kweb.io/api/kweb-core/kweb/-element/index.html) has two similar functions that
are convenience wrappers around these WebBrowser functions.

Note that this is unrelated to Kotlin's ability to [compile to JavaScript](https://kotlinlang.org/docs/js-overview.html).

## Calling a JavaScript function

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/js.kt:alert}}
```

## Calling with parameters

You can pass parameters to a JavaScript function by passing them as arguments to the `callJsFunction()` function,
using `{}` for substitution:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/js.kt:parameters}}
```

Parameters must be converted to a [JsonElement](https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-json/kotlinx.serialization.json/-json-element/),
for example by using the [json](https://docs.kweb.io/api/kweb-core/kweb.util/json.html) extension property.

## Function caching and preloading

Kotlin automatically caches JavaScript functions in the browser for efficiency. If the function is 
first called during initial page render, it will be parsed and cached as part of the initial page load.

## Calling a JavaScript function with a result

You can also retrieve a result from a function call using [callJsFunctionWithResult()](https://docs.kweb.io/api/kweb-core/kweb/-web-browser/call-js-function-with-result.html):

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/js.kt:with_result}}
```

`callJsFunctionWithResult()` is a suspend function so it must be called inside a [CoroutineScope](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/). 
You can create one within Kweb's DSL using **kwebScope()**. This scope will be cancelled automatically
when this part of the DOM is no-longer needed.

# JavaScript Interop

<!-- toc -->

## Introduction

Kweb's DOM interaction functionality is build on top of two functions that allow you to interact
directly with the browser's JavaScript interpreter:

* [WebBrowser.callJsFunction()](https://docs.kweb.io/api/kweb-core/kweb/-web-browser/call-js-function.html)
* [WebBrowser.callJsFunctionWithResult()](https://docs.kweb.io/api/kweb-core/kweb/-web-browser/call-js-function-with-result.html)

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
for example by using the [json](https://docs.kweb.io/api/kweb-core/kweb.util/json.html) extension property. Within
the JavaScript code the `{}` should be treated like a variable, so `alert({})` is ok but `alert("{}")` will not work.

## Function caching and preloading

Kotlin automatically caches JavaScript functions in the browser for efficiency. If the function is 
first called during initial page render, it will be parsed and cached as part of the initial page load.

## Calling a JavaScript function with a result

You can also retrieve a result from a function call using [callJsFunctionWithResult()](https://docs.kweb.io/api/kweb-core/kweb/-web-browser/call-js-function-with-result.html).
Note that the last line of the `jsBody` parameter must be a `return` statement:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/js.kt:with_result}}
```

`callJsFunctionWithResult()` is a suspend function so it must be called inside a [CoroutineScope](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/). 
You can create one within Kweb's DSL using **elementScope()**. This scope will be cancelled automatically
when this part of the DOM is no-longer needed.

## Including static JavaScript files

The [JavascriptPlugin](https://docs.kweb.io/api/kweb-core/kweb.plugins.javascript/-javascript-plugin/index.html) can be
used to conveniently add multiple static JavaScript files to your website, just add it to your resources folder as
follows:

```
├── src
│  └─── main
│      └─── resources
│          └─── script
│              └── test.js
```

Next add the plugin via the [plugins](https://docs.kweb.io/api/kweb-core/kweb/-kweb/index.html) constructor
parameter.

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/style.kt:JavascriptPlugin}}
```

Specify the relative path to the folder inside src/main/resources
and the files to include (either a single file name or a list of file names).

The files will be served under /kweb_static/js and linked from the websites
HTML head tag, for example:

```html
<script type="text/javascript" src="/kweb_static/js/test.js"></script>
```

# Observer Pattern & State

<!-- toc -->

## Overview

Kweb uses the [observer
pattern](https://en.wikipedia.org/wiki/Observer_pattern) to manage
state.

A Kweb app can be viewed as a mapping function between state on the
server and the DOM within the end-user's web browser. Once this mapping
is defined, simply modify this state and the change will propagate
automatically to the browser.

## The KVar Class

A
[KVar](https://docs.kweb.io/api/kweb-core/kweb.state/-k-var/index.html)
is an observable container. It contains a single typed object, which can change over time, similar to an 
[AtomicReference](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicReference.html). 
You can add listeners to a KVar to be notified immediately when it changes.

For example:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/state.kt:createkvar}}
```

Here we create a counter of type *KVar\<Int\>* initialized with the
value 0.

We can also read and modify the value of a KVar:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/state.kt:modifykvar}}
```

Will print:

```text
Counter value 0
Counter value 1
Counter value 2
```

KVars support powerful mapping semantics to create new KVars:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/state.kt:mapkvar}}
```

Will print:

```text
counter: 5, doubled: 10
counter: 6, doubled: 12
```

Note that `counterDoubled` updates automatically, because mapped KVars listen to the original for changes.

The `KVar` class is a subclass of [KVal](https://docs.kweb.io/api/kweb-core/kweb.state/-k-val/index.html), 
which is a read-only version of `KVar`.

*Note:* KVars should only be used to store values that are themselves immutable, such as an Int, String, or a 
Kotlin [data class](https://kotlinlang.org/docs/reference/data-classes.html) with immutable parameters.

## KVars and the DOM

You can use a KVar (or KVal) to set the text of a DOM element:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/state.kt:kvartext}}
```

The neat part is that if the value of *name* changes, the DOM element
text will update automatically. It may help to think of this as a way of
\"unwrapping\" a KVar.

Numerous other functions on
[Elements](https://jitpack.io/com/github/kwebio/core/0.3.15/javadoc/io.kweb.dom.element/-element/index.html)
support KVars in a similar manner, including
[innerHtml()](https://jitpack.io/com/github/kwebio/core/0.3.15/javadoc/io.kweb.dom.element/-element/inner-h-t-m-l.html)
and
[setAttribute()](https://jitpack.io/com/github/kwebio/core/0.3.15/javadoc/io.kweb.dom.element/-element/set-attribute.html).

## Binding a KVar to an input element's value

For \<input\> elements you can set the value to a KVar, which will
connect them bidirectionally.

Any changes to the KVar will be reflected in realtime in the browser,
and similarly any changes in the browser by the user will be reflected
immediately in the KVar, for example:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/state.kt:mapkvar}}
```

This will also work for `<option>` and `<textarea>` elements which also have values.

See also:
[ValueElement.value](https://docs.kweb.io/api/kweb-core/kweb/-value-element/index.html#-178499702%2FProperties%2F769193423)

## Rendering state to a DOM fragment

But what if you want to do more than just modify a single element based
on a KVar, what if you want to modify a whole tree of elements?

This is where the
[render](https://docs.kweb.io/api/kweb-core/kweb.state/render.html)

function comes in:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/state.kt:render1}}
```

Here, if we were to change the list:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/state.kt:render2}}
```

Then the relevant part of the DOM will be redrawn instantly.

The simplicity of this mechanism may disguise how powerful it is, since
render {} blocks can be nested, it's possible to be very selective
about what parts of the DOM must be modified in response to changes in
state.

**Note:** Kweb will only re-render a DOM fragment if the value of the KVar
actually changes so you should avoid \"unwrapping\" KVars with a
*render()* or *.text()* call before you need to.

The [KVal.map {}](https://javadoc.jitpack.io/com/github/kwebio/core/0.3.15/javadoc/io.kweb.state/-k-val/map.html)
function is a powerful tool for manipulating KVals and KVars without unwrapping them.

## Rendering lists with renderEach

The `renderEach()` function allows you to render a list of items, while 
automatically updating the rendered DOM in response to changes in the list. 

While a `KVar<List<FooBar>>` could be passed to `render()`, it would be
very inefficient because the entire list would be re-rendered every time.
`renderEach()` will only re-render the elements that have changed.

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/state.kt:renderEach}}
```

The items must be stored in an `ObservableList`, which implements the 
`MutableList` interface.

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/state.kt:rendereach}}
```

## Extracting data class properties

If your KVar contains a [data
class](https://kotlinlang.org/docs/reference/data-classes.html) then you
can use Kvar.property() to create a KVar from one of its properties
which will update the original KVar if changed:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/state.kt:dataclass}}
```

## Reversible mapping

If you check the type of *counterDoubled*, you'll notice that it's a
*KVal* rather than a *KVar*, meaning it cannot be modified. This will
result in a compilation error:

```kotlin
val counter = KVar(0)
val counterDoubled = counter.map { it * 2 }
counterDoubled.value = 20 // <--- This won't compile
```

The *KVar* class has a second
[map()](https://docs.kweb.io/api/kweb-core/kweb.state/-k-var/map.html)
function which takes a *ReversibleFunction* implementation. This version
of *map* will produce a KVar which can be modified, as follows:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/state.kt:reversible1}}
```

Reversible mappings are an advanced feature that you only need if you
want the mapped value to be a mutable KVar. Most of the time the simple
[KVal.map {}](https://docs.kweb.io/api/kweb-core/kweb.state/-k-val/map.html)
function is what you need.


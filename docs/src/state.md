# Observer Pattern & State

## Overview

Kweb uses the [observer
pattern](https://en.wikipedia.org/wiki/Observer_pattern) to manage
state.

A Kweb app can be viewed as a mapping function between state on the
server and the DOM within the end-user's web browser. Once this mapping
is defined, simply modify this state and the change will propagate
automatically to the browser.

## Building blocks

A
[KVar](https://github.com/kwebio/kweb-core/blob/master/src/main/kotlin/kweb/state/KVar.kt)
class contains a single typed object, which can change over time. For
example:

```kotlin
val counter = KVar(0)
```

Here we create a counter of type *KVar\<Int\>* initialized with the
value 0.

We can also read and modify the value of a KVar:

```kotlin
println("Counter value ${counter.value}")
counter.value = 1
println("Counter value ${counter.value}")
counter.value++
println("Counter value ${counter.value}")
```

Will print:

```text
Counter value 0
Counter value 1
Counter value 2
```

KVars support powerful mapping semantics to create new KVars:

```kotlin
val counterDoubled = counter.map { it * 2 }
counter.value = 5
println("counter: ${counter.value}, doubled: ${counterDoubled.value}")
counter.value = 6
println("counter: ${counter.value}, doubled: ${counterDoubled.value}")
```

Will print:

```text
counter: 5, doubled: 10
counter: 6, doubled: 12
```

Note that counterDoubled updates automatically.

::: note
::: title
Note
:::

KVars should only be used to store values that are themselves immutable,
such as an Int, String, or a Kotlin [data
class](https://kotlinlang.org/docs/reference/data-classes.html) with
immutable parameters.
:::

## KVars and the DOM

You can use a KVar (or KVal) to set the text of a DOM element:

```kotlin
val name = KVar("John")
li().text(name)
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
Kweb(port = 2395) {
    doc.body {
         p().text("What is your name?")
        val clickMe = input(type = text)
        val nameKVar = KVar("Peter Pan")
        clickMe.value = nameKVar
        p().text(nameKVar.map { n -> "Hi $n!" })
    }
}
```

This will also work for \<option\> and \<textarea\> elements which also
have values.

See also:
[ValueElement.value](https://github.com/kwebio/kweb-core/blob/master/src/main/kotlin/kweb/prelude.kt#L232)

## Rendering state to a DOM fragment

But what if you want to do more than just modify a single element based
on a KVar, what if you want to modify a whole tree of elements?

This is where the
[render](https://jitpack.io/com/github/kwebio/core/0.3.15/javadoc/io.kweb.state.persistent/render.html)
function comes in:

```kotlin
val list = KVar(listOf("one", "two", "three"))

Kweb(port = 16097) {
    doc.body {
        render(list) { rList ->
            ul {
                for (item in rList) {
                    li().text(item)
                }
            }
        }
    }
}
```

Here, if we were to change the list:

```kotlin
list.value = listOf("four", "five", "six")
```

Then the relevant part of the DOM will be redrawn instantly.

The simplicity of this mechanism may disguise how powerful it is, since
render {} blocks can be nested, it's possible to be very selective
about what parts of the DOM must be modified in response to changes in
state.

::: note
::: title
Note
:::

Kweb will only re-render a DOM fragment if the value of the KVar
actually changes so you should avoid \"unwrapping\" KVars with a
*render()* or *.text()* call before you need to.

The [KVal.map
{}](https://javadoc.jitpack.io/com/github/kwebio/core/0.3.15/javadoc/io.kweb.state/-k-val/map.html)
function is a powerful tool for manipulating KVals and KVars without
unwrapping them.
:::

## Extracting data class properties

If your KVar contains a [data
class](https://kotlinlang.org/docs/reference/data-classes.html) then you
can use Kvar.property() to create a KVar from one of its properties
which will update the original KVar if changed:

```kotlin
data class User(val name : String)
val user = KVar(User("Ian"))
val name = user.property(User::name)
name.value = "John"
println(user) // Will print: KVar(User(name = "John"))
```

## Reversible mapping

If you check the type of *counterDoubled*, you```ll notice that it```s a
*KVal* rather than a *KVar*.
[KVal](https://jitpack.io/com/github/kwebio/core/0.3.15/javadoc/io.kweb.state/-k-val/index.html)```s
values may not be modified directly, so this won```t be permitted:

```kotlin
val counter = KVar(0)
val counterDoubled = counter.map { it * 2 }
counterDoubled.value = 20 // <--- This won't compile
```

The *KVar* class has a second
[map()](https://jitpack.io/com/github/kwebio/core/0.3.15/javadoc/io.kweb.state/-k-var/map.html)
function which takes a *ReversibleFunction* implementation. This version
of *map* will produce a KVar which can be modified, as follows:

```kotlin
val counterDoubled = counter.map(object : ReversibleFunction<Int, Int>("doubledCounter") {
    override fun invoke(from: Int) = from * 2
    override fun reverse(original: Int, change: Int) = change / 2
})
counter.value = 5
println("counter: ${counter.value}, doubled: ${counterDoubled.value}")
// output: counter: 5, doubled: 10

counterDoubled.value = 12 // <-- Couldn't do this with a KVal
println("counter: ${counter.value}, doubled: ${counterDoubled.value}")
// output: counter: 6, doubled: 12
```

::: note
::: title
Note
:::

Reversible mappings are an advanced feature that you only need if you
want the mapped value to be a mutable KVar. Most of the time the simple
[KVal.map
{}](https://javadoc.jitpack.io/com/github/kwebio/core/0.3.15/javadoc/io.kweb.state/-k-val/map.html)
function will be what you need.
:::

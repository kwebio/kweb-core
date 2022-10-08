---
title: DOM Basics
---

# Creating DOM Elements and Fragments

Let\'s create a \<button\> as a child of the \<body\>:

```kotlin
import kweb.*

fun main() {
  Kweb(port = 16097) {
     doc.body {
         button().text("Click Me!")
     }
   }
 }
```

# Element Attributes

If you assign the button element to a val then you can also modify its
attributes:

```kotlin
val button = button()
button.text("Click Me!")
button.classes("bigbutton")
button.setAttribute("autofocus", JsonPrimitive(true))
```

Attributes can also be specified in a Map when you create the element:

```kotlin
button(mapOf("class" to "bigbutton", "autofocus" to true)).text("Click Me!")
```

Or delete it:

```kotlin
button.delete()
```

# Adding children to an existing element

The DSL syntax makes it very easy to create elements and their children
together:

```kotlin
ul {
  li().text("One")
  li().text("Two")
}
```

Alternatively we can use the [new {}]{.title-ref} function on Elements
to add children to a pre-existing Element:

```kotlin
val unorderedList : ULElement = ul()
unorderedList.new {
  li().text("One")
  li().text("Two")
}
```

# Reading from the DOM

Kweb can also read from the DOM, in this case the value of an \<input\>
element:

```kotlin
import kweb.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch

fun main() {
    Kweb(port = 2395) {
        doc.body {
            val input: InputElement = input()
            input.on(retrieveJs = input.valueJsExpression).submit { event ->
                 println("Value: ${event.retrieved}")
            }
        }
    }
}
```

Events can evaluate a JavaScript expression and send the result to the
server, in this case we give it an expression that will retrieve the
value of an InputElement, conveniently provided by valueJsExpression.

::: note
::: title
Note
:::

See the [Observer Pattern &
State](https://docs.kweb.io/en/latest/state.html#binding-a-kvar-to-an-input-element-s-value)
section for
:::

another way to read input element values.

# Supported HTML tags

Kweb supports a significant subset of HTML tags like *button()*, *p()*,
*a()*, *table()*, and so on. You can find a more complete list in
[prelude.kt](https://github.com/kwebio/kweb-core/blob/master/src/main/kotlin/kweb/prelude.kt)
(scroll down to the *Functions* section). This provides a nice
statically-typed HTML DSL, fully integrated with the Kotlin language.

If a tag doesn\'t have explicit support in Kweb that\'s not a problem.
For example, here is how you might use the infamous and now-obsolete
\<blink\> tag:

```kotlin
doc.body {
    val blink = element("blink").text("I am annoying!")
}
```

# Further Reading

The
[Element](https://github.com/kwebio/kweb-core/blob/master/src/main/kotlin/kweb/Element.kt)
class provides many other useful ways to interact with DOM elements.

# DOM Basics

## Table of Contents

<!-- toc -->

## Creating DOM Elements and Fragments

Let's create a `<button>` as a child of the `<body>` element and set its [text](https://docs.kweb.io/api/kweb-core/kweb/-element/text.html):

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/dom.kt:create1}}
```

The Kweb DSL can be used to create nested elements:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/dom.kt:create2}}
```

## Element Attributes

If you assign the button element to a val then you can also [set its
attributes](https://docs.kweb.io/api/kweb-core/kweb/-element/set.html):

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/dom.kt:setattributes}}
```

Or delete it:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/dom.kt:delete}}
```

## Adding children to an existing element

The DSL syntax makes it very easy to create elements and their children
together:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/dom.kt:children}}
```

The created
[Element](https://docs.kweb.io/api/kweb-core/kweb/-element/index.html) is passed to the
`{block}` as a parameter, which can be used to set attributes on the element, add
listeners, or set the element's [text](https://docs.kweb.io/api/kweb-core/kweb/-element/text.html) or [innerHtml](https://docs.kweb.io/api/kweb-core/kweb/-element/inner-h-t-m-l.html):

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/dom.kt:attr2}}
```

We can also use the `new {}` function to add children to a pre-existing Element:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/dom.kt:children_new}}
```

## Reading from the DOM

Kweb can also read from the DOM, in this case the value of an `<input>`
element:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/dom.kt:read_value}}
```

Events can evaluate a JavaScript expression and send the result to the
server, in this case we give it an expression that will retrieve the
value of an InputElement, conveniently provided by valueJsExpression.

**Note:** See the [Observer Pattern &
State](https://docs.kweb.io/book/state.html#binding-a-kvar-to-an-input-elements-value)
section for another way to read input element values.

## Supported HTML tags

Kweb supports a significant subset of HTML tags like *button()*, *p()*,
*a()*, *table()*, and so on. You can find a more complete list in
[prelude.kt](https://github.com/kwebio/kweb-core/blob/master/src/main/kotlin/kweb/prelude.kt)
(scroll down to the *Functions* section). This provides a nice
statically-typed HTML DSL, fully integrated with the Kotlin language.

If a tag doesn't have explicit support in Kweb that's not a problem.
For example, here is how you might use the infamous and now-obsolete
\<blink\> tag:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/dom.kt:blink}}
```

## Further Reading

The
[Element](https://github.com/kwebio/kweb-core/blob/master/src/main/kotlin/kweb/Element.kt)
class provides many other useful ways to interact with DOM elements.

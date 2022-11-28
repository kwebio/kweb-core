# Components

<!-- toc -->

## Managing Complexity

Composable components help manage software complexity by allowing developers to break down
a complex problem into smaller, more manageable pieces. Other benefits include
reusability, testability, and the ability to reason about a system in isolation.

## The Component typealias

We rely on a small amount of syntactic sugar defined in [kweb.components.Component](https://docs.kweb.io/api/kweb-core/kweb.components.html#7274344%2FClasslikes%2F769193423):

```kotlin
typealias Component = ElementCreator<*>
```

And then we can use an extension function on this to create a component:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/components.kt:simple_component}}
```

And we use the component like this:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/components.kt:component_usage}}
```

Components are configured through the extension fuunction parameters, typically through
a mixture of:

* [KVal](https://docs.kweb.io/api/kweb-core/kweb.state/-k-val/index.html)s for values that can change
* [KVar](https://docs.kweb.io/api/kweb-core/kweb.state/-k-var/index.html)s for values that can change or be changed by the Component
* [ObservableList](https://docs.kweb.io/api/kweb-core/kweb.state/-observable-list/index.html)s for lists of values that can change
* Other normal classes like **String** for immutable values

The simplest Component's may have no parameters at all, or just one or two, while the most complex might use 
a [DSL builder](https://in-kotlin.com/design-patterns/builder-pattern/dsl/).

## A more complex example

In this example we create a **Component** that wraps an [\<input\> element](https://bulma.io/documentation/form/input/)
styled using the [Bulma CSS framework](https://bulma.io/):

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/components.kt:bulma_component_example}}
```

This component can then be used like this:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/components.kt:bulma_component_usage}}
```

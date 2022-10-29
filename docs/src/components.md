# Managing Complexity 

Composable components help manage complexity in a software project by allowing developers to break down
a complex problem into smaller, more manageable pieces.

## Components

**Component** ([api](https://docs.kweb.io/api/kweb-core/kweb.state/-component/index.html?query=interface%20Component%3CR%3E)) 
is a simple but powerful abstraction to create reusable UI components in Kweb.

```kotlin
{{#include ../../src/main/kotlin/kweb/state/render.kt:component_definition}}
```

This component can then be passed to [ElementCreator.render](https://docs.kweb.io/api/kweb-core/kweb.state/render.html):

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/components.kt:component_usage}}
```

### A Simple Example

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/components.kt:simple_component}}
```

Components are configured through constructor parameters, typically through
a mixture of:

* [KVal](https://docs.kweb.io/api/kweb-core/kweb.state/-k-val/index.html)s for values that can change
* [KVar](https://docs.kweb.io/api/kweb-core/kweb.state/-k-var/index.html)s for values that can change or be changed by the Component
* [ObservableList](https://docs.kweb.io/api/kweb-core/kweb.state/-observable-list/index.html)s for lists of values that can change
* Other normal classes like **String** for immutable values

The simplest Component's may have no constructor at all, or just one or two, 
parameters, while the most complex might use a [DSL builder](https://in-kotlin.com/design-patterns/builder-pattern/dsl/).

**Component**'s can be rendered by calling their **render()** method, which can return
a generically typed value (or **Unit** if no value is returned).

### A more complex example

In this example we create a **Component** that wraps an [\<input\> element](https://bulma.io/documentation/form/input/)
styled using the [Bulma CSS framework](https://bulma.io/):

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/components.kt:component_input_example}}
```
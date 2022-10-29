# Managing Complexity with Components

`Component` is a simple but powerful abstraction to create reusable user interface
components in Kweb, it has a simple interface:

```kotlin
It has a single method [kweb.state.Component.render]:

```kotlin
{{#include ../../src/main/kotlin/kweb/state/render.kt:component_definition}}
```

### A Simple Example

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/components.kt:simple_component}}
```

`Component`s are configured through their constructors, typically through
a mixture of:

* `KVal`s for values that can change
* `KVar`s for values that can change or be changed by the Component
* `ObservableList`s for lists of values that can change
* Other normal classes like `String`, for things that are immutable

The simplest `Component`s may have no constructor at all, or just one or two, 
parameters, while the most complex might use a sophisticated [DSL builder](https://in-kotlin.com/design-patterns/builder-pattern/dsl/).

`Component`s can be rendered by calling their `render` method, which can return
a generically typed value (or `Unit` if no value is returned).

### A more complex example

In this example we create a `Component` that wraps an [\<input\> element](https://bulma.io/documentation/form/input/)
styled using the [Bulma CSS framework](https://bulma.io/):

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/components.kt:component_with_state}}
```

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/components.kt:component_input_example}}
```
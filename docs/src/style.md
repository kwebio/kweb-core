# CSS & Style

Kweb has out-of-the-box support for the excellent [Fomantic
UI](https://fomantic-ui.com) framework, which helps create beautiful,
responsive layouts using human-friendly HTML.

## Fomantic UI

### Getting started

First tell Kweb to use the Fomantic UI plugin:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/style.kt:plugin}}
```

Now the plugin will add the Fomantic CSS and JavaScript code to your
website automatically.

Let's look at one of the simple examples from the [Fomantic
UI](https://Fomantic-ui.com/elements/input.html) documentation:

```html
<div class="ui icon input">
  <input type="text" placeholder="Search...">
  <i class="search icon"></i>
</div>
```

This translates to the Kotlin:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/style.kt:search}}
```

Take a look at the [Fomantic UI documentation](https://fomantic-ui.com)
to see everything else it can do.

### Example and Demo

* [freenet.org](https://github.com/freenet/freenetorg-website/)
  A Kweb website built on Google Cloud Platform with Fomantic styling.


## Other UI Frameworks

Kweb is known to work well with a number of other CSS frameworks, including:

 * [Bulma](https://bulma.io/)

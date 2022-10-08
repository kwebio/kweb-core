# CSS & Style

Kweb has out-of-the-box support for the excellent [Fomantic
UI](https://fomantic-ui.com) framework, which helps create beautiful,
responsive layouts using human-friendly HTML.

## Getting started

First tell Kweb to use the Fomantic UI plugin:

```kotlin
import kweb.plugins.fomanticUI.*

fun main() {
    Kweb(port = 16097, plugins = listOf(fomanticUIPlugin)) {
        // ...
    }
}
```

Now the plugin will add the Fomantic CSS and JavaScript code to your
website automatically.

Let```s look at one of the simple examples from the [Fomantic
UI](https://Fomantic-ui.com/elements/input.html) documentation:

```html
<div class="ui icon input">
  <input type="text" placeholder="Search...">
  <i class="search icon"></i>
</div>
```

This translates to the Kotlin:

```kotlin
import kweb.plugins.fomanticUI.*
import kweb.dom.element.creation.tags.InputType.*

fun main() {
    Kweb(port = 16097, plugins = listOf(fomanticUIPlugin)4) {
        div(fomantic.ui.icon.input).new {
            input(type = text, placeholder = "Search...")
            i(fomantic.search.icon)
        }
    }
}
```

Take a look at the [Fomantic UI documentation](https://fomantic-ui.com)
to see everything else it can do.

## Other UI Frameworks

Kweb is known to work well with a number of other CSS frameworks -
particularly those that work through pure HTML, such as
[Tailwind](https://tailwindcss.com/).

## Example and Demo

* [freenet.org](https://github.com/freenet/freenetorg-website/)
  A Kweb website built on Google Cloud Platform with Fomantic styling.

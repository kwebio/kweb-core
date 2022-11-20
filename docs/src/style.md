# CSS & Style

Kweb can integrate easily with most CSS frameworks, particularly those
that don't have a heavy reliance on JavaScript. Injecting custom CSS files 
is also supported by using the CSSPlugin.

<!-- toc -->

## CSS Frameworks

### Fomantic UI

Kweb has out-of-the-box support for the excellent [Fomantic
UI](https://fomantic-ui.com) framework, which helps create beautiful,
responsive layouts using human-friendly HTML.

#### Getting started

First tell Kweb to use the Fomantic UI plugin:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/style.kt:fomanticUIPlugin}}
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

#### Example and Demo

* [freenet.org](https://github.com/freenet/freenetorg-website/)
  A Kweb website built on Google Cloud Platform with Fomantic styling.


### Other UI Frameworks

Kweb is known to work well with a number of other CSS frameworks, including:

 * [Bulma](https://bulma.io/)

## Custom CSS files
The CSSPlugin can be used to inject custom CSS files into the website. To do so, 
create your .css files in a folder located in src/main/resources and 
add the CSSPlugin to the list of plugins.

As an example, for the folder structure

```
├── src
│  └─── main
│      └─── resources
│          └─── css
│              └── test.css
```

the plugin definition would look like this:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/style.kt:CSSPlugin}}
```

You just need to specify the relative path to the folder inside src/main/resources
and the files to include (either a single file name or a list of file names). 
The files will then be served under /kweb_static/css and linked in the websites 
HTML head tag:

```html
<link rel="stylesheet" type="text/css" href="/kweb_static/css/test.css">
```
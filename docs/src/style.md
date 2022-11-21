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

The [CSSPlugin](https://docs.kweb.io/api/kweb-core/kweb.plugins.css/-c-s-s-plugin/index.html) can be used to 
conveniently add multiple CSS files to your website, just add it to your resources folder as follows:

```
├── src
│  └─── main
│      └─── resources
│          └─── css
│              └── test.css
```

Next add the plugin via the [plugins](https://docs.kweb.io/api/kweb-core/kweb/-kweb/index.html) constructor
parameter.

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/style.kt:CSSPlugin}}
```

Specify the relative path to the folder inside src/main/resources
and the files to include (either a single file name or a list of file names). 

The files will be served under /kweb_static/css and linked from the websites 
HTML head tag, for example:

```html
<link rel="stylesheet" type="text/css" href="/kweb_static/css/test.css">
```
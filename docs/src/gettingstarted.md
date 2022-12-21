# Getting Started

<!-- toc -->

## Requirements

To get started with Kweb, you should have some familiarity with [Kotlin](https://kotlinlang.org/) and 
[Gradle](https://gradle.org/). It is also helpful to have a basic understanding of HTML.

## Adding Kweb to your Gradle project

To add Kweb to your Gradle project, you will need to include the following dependencies in your 
`build.gradle` or `build.gradle.kt` files:

### Groovy

```kotlin
dependencies {
  implementation 'io.kweb:kweb-core:KWEB_VERSION'

  // This (or another SLF4J binding) is required for Kweb to log errors
  implementation 'org.slf4j:slf4j-simple:2.0.3'
}
```

### Kotlin

```kotlin
dependencies {
  implementation("io.kweb:kweb-core:KWEB_VERSION")

  // This (or another SLF4J binding) is required for Kweb to log errors
  implementation("org.slf4j:slf4j-simple:2.0.3")
}
```

## Hello world

To create a simple "Hello World" example with Kweb, create a new Kotlin file 
and enter the following code:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/gettingstarted.kt:hello_world}}
```

Run the file and then visit <http://localhost:16097/> in your web browser to see the 
output. This should display the following HTML body:

```html
<body>
  <h1>Hello World!</h1>
</body>
```

This example demonstrates two important features of Kweb:

 * Setting up a kwebsite is easy and does not require servlets or third-party web servers.
 * The structure of your Kotlin code will closely match the structure of the HTML it generates.

## Hello world²

One way to think of Kweb is as a [domain-specific language
(DSL)](https://en.wikipedia.org/wiki/Domain-specific_language) for
building and manipulating a
[DOM](https://en.wikipedia.org/wiki/Document_Object_Model) in a remote
web browser, while also listening for and handing DOM events. It's important to note
that this DSL can also do anything that Kotlin can do, including
features like for loops, functions, coroutines, and classes.

Here is a simple example using a Kotlin *for loop*:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/gettingstarted.kt:hello_world_2}}
```

This will produce the following HTML:

```html
<body>
  <ul>
      <li>Hello World 1!</li>
      <li>Hello World 2!</li>
      <li>Hello World 3!</li>
      <li>Hello World 4!</li>
      <li>Hello World 5!</li>
  </ul>
</body>
```

## Template Repository

You can find a simple template Kweb project in
[kwebio/kweb-template](https://github.com/kwebio/kweb-template).

# Getting Started

## Requirements

Some familiarity with [Kotlin](https://kotlinlang.org/) is assumed, as
is familiarity with [Gradle](https://gradle.org/). You should also have
some familiarity with HTML.

## Adding Kweb to your Gradle project

Add these to your repositories and dependencies in your `build.gradle` or `build.gradle.kt` files.

### Gradle

#### Groovy

```kotlin
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}
```

```kotlin
dependencies {
  implementation 'com.github.kwebio:kweb-core:KWEB_VERSION'

  // This (or another SLF4J binding) is required for Kweb to log errors
  implementation 'org.slf4j:slf4j-simple:2.0.3'
}
```

#### Kotlin

```kotlin
repositories {
  mavenCentral()
  maven {
      url = uri("https://jitpack.io")
  }  
}
```

```kotlin
dependencies {
  implementation("com.github.kwebio:kweb-core:KWEB_VERSION")

  // This (or another SLF4J binding) is required for Kweb to log errors
  implementation("org.slf4j:slf4j-simple:2.0.3")
}
```

## Hello world

Create a new Kotlin file and type this:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/gettingstarted.kt:hello_world}}
```

Run it, and then visit <http://localhost:16097/> in your web browser to
see the traditional greeting, translating to the following HTML body:

```html
<body>
  <h1>Hello World!</h1>
</body>
```

This simple example already illustrates some important features of Kweb:

-   Getting a kwebsite up and running is a breeze, no messing around
    with servlets, or third party webservers
-   Your Kweb code will loosely mirror the structure of the HTML it
    generates

## Hello world²

One way to think of Kweb is as a [domain-specific language
(DSL)](https://en.wikipedia.org/wiki/Domain-specific_language) for
building and manipulating a
[DOM](https://en.wikipedia.org/wiki/Document_Object_Model) in a remote
web browser, while also listening for and handing DOM events.

Importantly, this DSL can also do anything Kotlin can do, including
features like for loops, functions, coroutines, and classes.

Here is a simple example using an ordinary Kotlin *for loop*:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/gettingstarted.kt:hello_world_2}}
```

To produce\...

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

You can use functions for modularization and reuse:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/gettingstarted.kt:hello_world_3}}
```

As you can see this is an extension function, which allows you to use
the Kweb DSL within the newly created function.

Don't worry if you```re unsure about this because you can use
IntelliJ```s [extract
function](https://www.jetbrains.com/help/idea/extract-method.html)
refactoring to create these functions automatically.

## Template Repository

You can find a simple template Kweb project in
[kwebio/kweb-template](https://github.com/kwebio/kweb-template).

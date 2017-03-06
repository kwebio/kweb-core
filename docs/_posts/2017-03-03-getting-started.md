---
layout: page
title: "Getting Started"
category: use
date: 2017-03-03 09:26:50
js:
- deps/mavenrepo.js
---

1. TOC
{:toc}

### What you should know

KWeb is built on [Kotlin 1.1](http://kotlinlang.org/), you should have some familiarity with Kotlin
and the Java ecosystem on which it's built.

### Adding a KWeb dependency

The KWeb library is distributed via [JitPack](https://jitpack.io/#sanity/kweb), it can be added
easily to almost any Java project:

#### Gradle 
For [Gradle](http://www.gradle.org/) users, add this to the repositories section of your `build.gradle`:
```groovy
  repositories {
    // ...
    maven { url 'https://jitpack.io' }
    maven { url "http://dl.bintray.com/kotlin/kotlin-eap-1.1" }
    maven { url "https://dl.bintray.com/wasabifx/wasabifx" }
    }
```

Then add this to the dependencies section of your `build.gradle`:
```groovy
dependencies {
  // ...
  compile 'com.github.sanity:kweb:MAVEN_VERSION_PLACEHOLDER'
}
```

#### Kobalt

For [Kobalt](http://beust.com/kobalt/) users, add this to your `Built.kt` file:

```kotlin
  buildScript {
    // ...
    repos(
        "https://jitpack.io", 
        "http://dl.bintray.com/kotlin/kotlin-eap-1.1", 
        "https://dl.bintray.com/wasabifx/wasabifx"
        )
    // ...
  }
```

```kotlin
  dependencies {
    // ...
    compile("com.github.sanity:kweb:MAVEN_VERSION_PLACEHOLDER")
    // ...
  }
```

#### Maven
For [Maven](https://maven.apache.org/) users, add this to the repositories section of your `pom.xml`:
```xml
<repositories>
<repository><id>jitpack.io</id><url>https://jitpack.io</url></repository>
<repository><snapshots><enabled>false</enabled></snapshots>
    <id>dl</id><name>bintray</name><url>http://dl.bintray.com/kotlin/kotlin-eap-1.1</url>
</repository>
<repository><id>wasabifx</id><url>https://dl.bintray.com/wasabifx/wasabifx/</url></repository>
</repositories>
```

Then add this to the dependencies section of your `pom.xml`:
```xml
<dependency>
  <groupId>com.github.sanity</groupId>
  <artifactId>kweb</artifactId>
  <version>MAVEN_VERSION_PLACEHOLDER</version>
</dependency>  
```

### Your first KWeb page

Add a file like the following to your project:

```kotlin
fun main(args: Array<String>) {
    KWeb(port = 7823) {
        doc.body.h1().setText("Hello World!")
    }
}
```

Run the file and visit [http://localhost:7823/](http://localhost:7823/) in your web browser, ta-da!

### A more ambitious example

Let's edit the file to do something more interesting:

```kotlin
fun main(args: Array<String>) {
    KWeb(port = 7823) {
        var counter = 0
        val h1 = doc.body.h1()
        h1.setText("Hello World!")
        h1.on.click {
            h1.setText(counter.toString())
            counter++
        }
    }
}
```

Here we create a header-1 element as before, and set its text to "Hello World!" as before also.  In this example
we've assigned the header element to a variable called `h1`, and then we use the variable to set the text.

Next we create a click event listener on the header element, once clicked we set the text of the element to the value
of the variable `counter`, and increase the value of `counter`.

Kill KWeb if it is still running, and run this new version.  Try clicking on the text.

**Troubleshooting**: If you get an error like `Exception in thread "main" java.net.BindException: Address already in use` it means 
that the previous version is still running and therefore the new version is unable to listen on port 7823, make
sure you've killed it.
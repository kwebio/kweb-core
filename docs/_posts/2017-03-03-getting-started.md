---
layout: page
title: "Setting up"
category: use
order: 1
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

-----------

**Next: [Your First Kwebsite]({{ site.baseurl }}{% post_url 2017-03-08-your-first-kwebsite %}) >>>>**

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

#### Maven
For Maven users, add this to the repositories section of your `pom.xml`:
```xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>
```

Then add this to the dependencies section of your `pom.xml`:
```xml
<dependency>
  <groupId>com.github.sanity</groupId>
  <artifactId>kweb</artifactId>
  <version>MAVEN_VERSION_PLACEHOLDER</version>
</dependency>  
```

#### Gradle
For Gradle users, add this to the repositories section of your `build.gradle`:
```groovy
  repositories {
    // ...
    maven { url 'https://jitpack.io' }
    }
```

Then add this to the dependencies section of your `build.gradle`:
```groovy
dependencies {
  // ...
  compile 'com.github.sanity:kweb:MAVEN_VERSION_PLACEHOLDER'
}
```

### Your first KWeb page

Add a file like the following to your project:



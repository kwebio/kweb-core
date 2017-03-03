---
layout: page
title: "Getting Started"
category: use
date: 2017-03-03 09:26:50
jsarr:
- js/mavenrepo.js
---

### What do you need to know?

KWeb is built on [Kotlin 1.1](http://kotlinlang.org/), you should have some familiarity with Kotlin
and the Java ecosystem on which it's built.

### Adding a KWeb dependency

#### Maven
```xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>
```

```xml
{% include deps/maven.html}
```
#### Gradle
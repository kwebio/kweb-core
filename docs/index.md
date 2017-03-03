---
layout: default
title: "Jekyll Docs Template"
---

1. TOC
{:toc}

#### What is KWeb?

KWeb is a library for building rich web applications in the [Kotlin](http://kotlinlang.org/)
programming language.

#### Features
* Build rich websites in the awesome Kotlin programming language
* Makes the barrier between web browser and web server largely invisible
* Avoid the complexities of the javascript ecosystem
* Seamlessly integrates with powerful tools like JQuery, MDL, and Bootstrap
* Works with [HotSwapAgent](http://hotswapagent.org/) to update your web browser instantly in 
  response to code changes

#### How does it work?
KWeb keeps all of the logic server-side, and uses websockets to communicate with web browsers.
We also take advantage of Kotlin's powerful new coroutines mechanism to efficiently handle
asynchronicity largely invisible to the programmer.

#### What does it look like?

Check out the included [demos](https://github.com/sanity/kweb/tree/master/src/main/kotlin/com/github/sanity/kweb/demos).
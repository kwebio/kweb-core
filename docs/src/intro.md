https://ktor.io/# Introduction

## Why another web framework?

Modern websites consist of at least two [tightly
coupled](https://en.wikipedia.org/wiki/Coupling_(computer_programming))
components, one runs in the browser, the other on the server. These are
often written in different programming languages and must communicate
with each other over an HTTP(S) connection.

Kweb's goal is to eliminate this server/browser separation so you can
focus on building your website or user interface, not on the plumbing.

## What is Kweb?

Kweb is a remote interface to a web browser's DOM. With Kweb, you can create 
and manipulate DOM elements, and bind mutable values to DOM elements, and listen for 
and handle events. All of this can be done using an intuitive and convenient domain-specific 
language that mirrors the structure of the HTML being created. 

Kweb is very efficient, using many [optimizations](speed.md) to minimize latency and resource 
usage both on the server and in the browser. Kweb is built on the [Ktor](https://ktor.io/) 
framework, which handles HTTP, HTTPS, and WebSocket transport.

## Features

* End-to-end Kotlin - write your entire web site or user interface in Kotlin, without needing to communicate between a browser and server ([Why
    Kotlin?](https://steve-yegge.blogspot.com/2017/05/why-kotlin-is-better-than-whatever-dumb.html?m=1))
    
* Realtime synchronization of your back-end data with your web page - Kweb takes care of all the plumbing for you

* Server-side HTML rendering with [hydration](https://en.wikipedia.org/wiki/Hydration_(web_development)), Kweb can render your HTML on the server before sending it to the browser, so that the browser doesn't have to do any rendering

* Efficient instruction preloading - Kweb can avoid unnecessary server communication by preloading instructions

* Very lightweight - Kweb is less than 5,000 lines of code

## Relevant Links

* [Github repository](https://github.com/kwebio/kweb-core)
* [API documentation](https://docs.kweb.io/api/)
* [Example Google Cloud Project](https://github.com/freenet/freenetorg-website/)
* [Questions/Feedback/Bugs](https://github.com/kwebio/kweb-core/issues)
* Chat with us on [Matrix](https://matrix.to/#/#kweb:matrix.org)

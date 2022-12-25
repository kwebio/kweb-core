# Introduction

## Why another web framework?

Kweb is designed to make it easy for developers to create modern websites without having to worry about 
the complexities of communication between the server and the browser. With a unified codebase, you can 
focus on creating an intuitive and user-friendly interface, rather than spending time on technical details. 
By streamlining the development process, Kweb makes it easier to build functional and beautiful websites 
that meet the needs of your users.

## How doees it work?

Kweb is a remote interface for a web browser's DOM (Document Object Model). With Kweb, you can create 
and manipulate DOM elements, and listen for and handle events, all using an intuitive Kotlin DSL that mirrors 
the structure of the HTML being created. Kweb is built on the Ktor framework, which handles HTTP, HTTPS, and 
WebSocket transport, and is optimized to minimize latency and resource usage on both the server and browser.

## Features

* End-to-end Kotlin - Write your entire web site or user interface in Kotlin, without needing to communicate between a browser and server.
* Real-time synchronization of your back-end data with your web page - Kweb takes care of all the plumbing for you.
* Server-side HTML rendering with hydration - Kweb can render your HTML on the server before sending it to the browser, so the browser doesn't have to do any rendering.
* Efficient instruction preloading - Kweb can avoid unnecessary server communication by preloading instructions.
* Very lightweight - Kweb is less than 5,000 lines of code.

## Relevant Links

* [GitHub repository](https://github.com/kwebio/kweb-core)
* [API documentation](https://docs.kweb.io/api/)
* [Example Google Cloud Project](https://github.com/freenet/freenetorg-website/)
* [Questions/Feedback/Bugs](https://github.com/kwebio/kweb-core/issues)
* Chat with us on [Matrix](https://matrix.to/#/#kweb:matrix.org)

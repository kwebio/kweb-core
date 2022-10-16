# Introduction

## Motivation

Modern websites consist of at least two [tightly
coupled](https://en.wikipedia.org/wiki/Coupling_(computer_programming))
components, one runs in the browser, the other on the server. These are
often written in different programming languages and must communicate
with each other over an HTTP(S) connection.

Kweb's goal is to eliminate this server/browser separation so that your
webapp's architecture is determined by the problem you're solving,
rather than the limitations of today's tools.

## How does it work?

Kweb is a self-contained Kotlin library that can be added easily to new
or existing projects. When Kweb receives a HTTP request it responds with
the initial HTML page, and some JavaScript that connects back to the web
server via a WebSocket. The page then waits and listens for instructions
from the server, while notifying the server of relevant browser events.

A common concern about this approach is that the user interface might
feel sluggish if it is server driven. Kweb solves this problem by
[preloading](https://docs.kweb.io/en/latest/events.html#immediate-events)
instructions to the browser to be executed immediately on browser events
without a server round-trip.

Kweb is built on the excellent [Ktor](https://ktor.io/) framework, which
handles HTTP, HTTPS, and WebSocket transport. You don't need to know
Ktor to use Kweb, but if you've already got a Ktor app you can [embed
Kweb as a Feature](https://github.com/kwebio/kweb-demos/blob/master/ktorFeature/src/FeatureApp.kt).

## Features

-   Allows the problem to determine your architecture, not the
    server/browser divide
-   End-to-end Kotlin ([Why
    Kotlin?](https://steve-yegge.blogspot.com/2017/05/why-kotlin-is-better-than-whatever-dumb.html?m=1))
-   Keep the web page in sync with your back-end data in realtime, Kweb
    does all the plumbing for you
-   Server-side HTML rendering with
    [rehydration](https://developers.google.com/web/updates/2019/02/rendering-on-the-web)
-   Efficient instruction preloading to avoid unnecessary server
    communication
-   Very lightweight, Kweb is less than 5,000 lines of code

## Relevant Links

* [Github repository](https://github.com/kwebio/kweb-core)
* [API documentation](https://docs.kweb.io/api/)
* [Example Google Cloud Project](https://github.com/freenet/freenetorg-website/)
* [Questions/Feedback/Bugs](https://github.com/kwebio/kweb-core/issues)
* Chat with us on [Matrix](https://matrix.to/#/#kweb:matrix.org)

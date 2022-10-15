# Event Handling

## Listening for events

You can attach event handlers to DOM elements:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/events.kt:attach_1}}
```

Most if not all JavaScript event types are supported, and you can read
event data like which key was pressed:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/events.kt:read}}
```

## Immediate events

Since the code to respond to events runs on the server, there may be a
short but sometimes noticeable delay before the page updates in response
to an event.

Fortunately, Kweb has a solution:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/events.kt:immediate}}
```

Kweb executes this event handler *on page render* and records the
changes it makes to the DOM. It then \"pre-loads\" these instructions to
the browser such that they are executed immediately without a server
round trip.

**Warning:** Due to this pre-loading mechanism, the event handler for an
*onImmediate* must limit itself to simple DOM modifications. Kweb
includes some runtime safeguards against this but they can't catch
everything so please use with caution.

## Combination event handlers

A common pattern is to use both types of event handler on a DOM element.
The immediate handler might disable a clicked button, or temporarily
display some form of [spinner](https://loading.io/css/). The normal
handler would then do what it needs on the server, and then perhaps
re-enable the button and remove the spinner.

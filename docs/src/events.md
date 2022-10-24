# Event Handling

<!-- toc -->

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
{{#include ../../src/test/kotlin/kweb/docs/events.kt:retrieveJs}}
```

Kweb executes this event handler *on page render* and records the
changes it makes to the DOM. It then \"pre-loads\" these instructions to
the browser such that they are executed immediately without a server
round trip.

**Warning:** Due to this pre-loading mechanism, the event handler for an
*onImmediate* must limit itself to simple DOM modifications. Kweb
includes some runtime safeguards against this but they can't catch
everything so please use with caution.

### Using events and immediate events together

A common pattern is to use both types of event handler on a DOM element.
The immediate handler might disable a clicked button, or temporarily
display some form of [spinner](https://loading.io/css/). The normal
handler would then do what it needs on the server, and then perhaps
re-enable the button and remove the spinner.

## Querying the DOM when an event is triggered

Sometimes you need to know the state of the DOM when an event is triggered.
You could query it from within the event handler but this would add a server 
round trip. 

Alternatively you can use [retrieveJs](https://docs.kweb.io/api/kweb-core/kweb.html.events/-on-receiver/-on-receiver.html). This will execute the JavaScript
expression you provide and return the result in the [kweb.html.events.Event.retrieved]
property of the event:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/events.kt:retrieveJs}}
```


For [ValueElement](https://docs.kweb.io/api/kweb-core/kweb/-value-element/index.html)s
such as [InputElement](https://docs.kweb.io/api/kweb-core/kweb/-input-element/index.html)
there is a convenience property `valueJsExpression` that you can use to retrieve
the current value of the element:

```kotlin
{{#include ../../src/test/kotlin/kweb/docs/events.kt:retrieveJs2}}
```
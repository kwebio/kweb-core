---
title: Event Handling
---

# Listening for events

You can attach event handlers to DOM elements:

``` kotlin
doc.body {
    val label = h1()
    label.text("Click Me")
    label.on.click {
        label.text("Clicked!")
    }
}
```

Most if not all JavaScript event types are supported, and you can read
event data like which key was pressed:

``` kotlin
doc.body {
    val input = input(type = text)
    input.on.keypress { keypressEvent ->
        println("Key Pressed: ${keypressEvent.key}")
    }
}
```

# Immediate events

Since the code to respond to events runs on the server, there may be a
short lag between the action causing the event and any changes to the
DOM caused by the event handler. This was a common complaint about
previous server-driven web frameworks like Vaadin, inhibiting their
adoption.

Fortunately, Kweb has a solution:

``` kotlin
doc.body {
    val label = h1()
    label.text("Click Me")
    label.onImmediate.click {
        label.text("Clicked!")
    }
}
```

This is identical to the first event listener example, except *on* has
been replaced by *onImmediate*.

Kweb executes this event handler *on page render* and records the
changes it makes to the DOM. It then \"pre-loads\" these instructions to
the browser such that they are executed immediately when the event
occurs without any server round-trip.

::: warning
::: title
Warning
:::

Due to this pre-loading mechanism, the event handler for an
*onImmediate* must limit itself to simple DOM modifications. Kweb
includes some runtime safeguards against this but they can\'t catch
every problem so please use with caution.
:::

# Combination event handlers

A common pattern is to use both types of event handler on a DOM element.
The immediate handler might disable a clicked button, or temporarily
display some form of [spinner](https://loading.io/css/). The normal
handler would then do what it needs on the server, and then perhaps
re-enable the button and remove the spinner.

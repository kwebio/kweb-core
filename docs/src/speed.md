# Speed & Efficiency

Kweb is designed to be fast and efficient, both in the browser and on the server. This chapter documents some
of the techniques Kweb uses to achieve this.

<!-- toc -->

## Server-side rendering

Kweb uses the excellent [JSoup](https://jsoup.org/) library to render the initial HTML page, which is supplied to
the browser when the page first loads. This leads to a much faster initial page load because the browser can 
start rendering the page before the JavaScript has loaded.

## Hydration

Kweb uses a technique called [hydration](https://en.wikipedia.org/wiki/Hydration_(web_development)) to add
JavaScript listeners to the DOM elements that were rendered by the server.

## WebSockets

After the initial page load Kweb uses WebSockets to communicate with the browser. This is more efficient than
HTTP because messages can be initiated by either the browser or the server, and the connection is kept open
between messages.

## Immediate events

Normal DOM events are reported to the Kweb server which then decides how to handle them. In some situations this
can cause a short but noticeable delay. [Immediate events](events.md#immediate-events) address this by allowing
the event handler's DOM changes to be "recorded" by Kweb and sent to the browser in advance so it can be executed
immediately.

## JavaScript caching

After the initial page load, Kweb modifies the DOM by sending JavaScript to the browser. Much of this JavaScript
is sent as part of the initial page load, but some is sent dynamically as the user interacts with the page.
This JavaScript is parsed using [Function](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Function)
(which is more efficient than [eval](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/eval)), 
and then cached in the browser for future use.

## Input field diffing

When an `<input>` or `<textarea>` field is modified, Kweb only sends the change to the browser rather than the
entire field. This is essential for performance when the field is large, or when its being synchronised on
every keypress.

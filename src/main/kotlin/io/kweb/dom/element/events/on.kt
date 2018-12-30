package io.kweb.dom.element.events

import io.kweb.dom.element.Element

/**
 * See [here](https://docs.kweb.io/en/latest/dom.html#listening-for-events).
*/
val Element.on: ONReceiver get() = ONReceiver(this)

/**
 * See [here](https://docs.kweb.io/en/latest/dom.html#immediate-events).
 */
val Element.onImmediate : ONImmediateReceiver get() = ONImmediateReceiver(this)
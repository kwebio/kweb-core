package io.kweb.dom.element.events

import io.kweb.dom.element.Element

/**
 * Created by ian on 1/13/17.
 */

/**
 * Add an event callback to the `Element` that will be triggered on the server.
 *
 * Pro:
 *   * Can do anything it wishes in the callback, including modifying server state.
 * Con:
 *   * Slight delay due to server-round trip for any DOM modifications in response to the event
 */
val Element.on: ONReceiver get() = ONReceiver(this)

/**
 * Add an event callback to the `Element` that will be triggered within the browser.
 *
 * *Warning:* Do not use this before familiarizing yourself with the information below.
 *
 * Pro:
 *   * Executes immediately, without any need for a server round-trip
 * Con:
 *   * What you can do in the callback shouldn't rely on and _certainly_ shouldn't modify any server-side state.  It's
 *     use should probably be limited to cosmetic UI cues like spinners.  If you do the results could be very
 *     unpredictable, and may cause bugs that are difficult to diagnose.
 *
 *     The reason is that the callback is actually only executed once.  Any instructions to the browser are recorded
 *     and will simply be re-executed by the browser whenever this event occurs.  This neat trick is how we avoid
 *     a server round-trip, but the price is additional caution about how we use it.
 */
val Element.onImmediate : ONImmediateReceiver get() = ONImmediateReceiver(this)
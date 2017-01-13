package com.github.sanity.kweb.dom.element.events

import com.github.sanity.kweb.dom.element.Element

/**
 * Created by ian on 1/13/17.
 */


val Element.on: ONReceiver get() = ONReceiver(this)

package com.github.sanity.kweb.dom.element.events

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.ElementCreator

/**
 * Created by ian on 1/13/17.
 */


val ElementCreator.on: ONReceiver get() = ONReceiver(this.element)

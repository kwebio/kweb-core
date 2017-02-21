package com.github.sanity.kweb.plugins.materialdesignlite.events

import com.github.sanity.kweb.dom.element.events.ONReceiver

/**
 * Created by ian on 1/24/17.
 */

fun ONReceiver.mdlComponentUpgraded(callback: (String) -> Unit) = event("mdl-componentupgraded", callback = callback)
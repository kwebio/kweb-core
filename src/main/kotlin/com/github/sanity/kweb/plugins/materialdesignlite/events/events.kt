package com.github.sanity.kweb.plugins.materialdesignlite.events

import com.github.sanity.kweb.RootReceiver
import com.github.sanity.kweb.dom.element.events.ONReceiver

/**
 * Created by ian on 1/24/17.
 */

fun ONReceiver.mdlComponentUpgraded(rh: RootReceiver.() -> Unit) = event("mdl-componentupgraded", rh)
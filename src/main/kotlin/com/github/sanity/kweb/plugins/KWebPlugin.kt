package com.github.sanity.kweb.plugins

import java.util.*

abstract class KWebPlugin(val dependsOn: Set<KWebPlugin> = Collections.emptySet()) {
    // TODO: Allow plugins to specify any other plugin that they should
    // TODO: run before or after, so that the user doesn't need to
    // TODO: worry about getting the order right.

    abstract fun decorate(startHead: StringBuilder, endHead: StringBuilder)
}
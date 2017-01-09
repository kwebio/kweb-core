package com.github.sanity.kweb.plugins

interface KWebPlugin {
    // TODO: Allow plugins to specify any other plugin that they should
    // TODO: run before or after, so that the user doesn't need to
    // TODO: worry about getting the order right.

    fun decorate(startHead: StringBuilder, endHead: StringBuilder)
}
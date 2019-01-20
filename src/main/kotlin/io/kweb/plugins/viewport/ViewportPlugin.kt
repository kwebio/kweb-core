package io.kweb.plugins.viewport

import io.kweb.plugins.KWebPlugin
import io.kweb.plugins.viewport.ViewportWidth.deviceWidth

class ViewportPlugin(val width : ViewportWidth = deviceWidth, val initialScale : Double = 1.0) : KWebPlugin() {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        startHead.append("<meta name=\"viewport\" content=\"width=${width.text}, initial-scale=$initialScale\" \\>")
    }
}

enum class ViewportWidth(val text : String) {
    deviceWidth("device-width")
}
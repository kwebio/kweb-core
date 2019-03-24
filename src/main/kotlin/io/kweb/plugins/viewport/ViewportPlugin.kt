package io.kweb.plugins.viewport

import io.kweb.plugins.KwebPlugin
import io.kweb.plugins.viewport.ViewportWidth.deviceWidth

class ViewportPlugin(val width : ViewportWidth = deviceWidth, val initialScale : Double = 1.0) : KwebPlugin() {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        startHead.append("<meta name=\"viewport\" content=\"width=${width.text}, initial-scale=$initialScale\" \\>")
    }
}

enum class ViewportWidth(val text : String) {
    deviceWidth("device-width")
}
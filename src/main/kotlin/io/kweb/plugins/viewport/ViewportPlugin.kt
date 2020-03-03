package io.kweb.plugins.viewport

import io.kweb.plugins.KwebPlugin
import io.kweb.plugins.viewport.ViewportWidth.deviceWidth
import org.jsoup.nodes.Document

class ViewportPlugin(val width : ViewportWidth = deviceWidth, val initialScale : Double = 1.0) : KwebPlugin() {
    override fun decorate(doc : Document) {
        doc.head().appendElement("meta")
                .attr("name", "viewport")
                .attr("content", "width=${width.text}, initial-scale=$initialScale")
    }
}

enum class ViewportWidth(val text : String) {
    deviceWidth("device-width")
}
package kweb.plugins.viewport

import kweb.plugins.KwebPlugin
import kweb.plugins.viewport.ViewportWidth.DeviceWidth
import org.jsoup.nodes.Document

class ViewportPlugin(val width: ViewportWidth = DeviceWidth, val initialScale: Double = 1.0) : KwebPlugin() {
    override fun decorate(doc: Document) {
        doc.head().appendElement("meta")
                .attr("name", "viewport")
                .attr("content", "width=${width.text}, initial-scale=$initialScale")
    }
}

enum class ViewportWidth(val text: String) {
    DeviceWidth("device-width")
}
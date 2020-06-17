package kweb.plugins.chartJs

import kweb.plugins.KwebPlugin
import org.jsoup.nodes.Document

class ChartJsPlugin : KwebPlugin() {
    override fun decorate(doc: Document) {
        doc.head().appendElement("script")
                .attr("type", "text/javascript")
                .attr("src", "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.2/Chart.bundle.min.js")
    }
}

val chartJs = ChartJsPlugin()


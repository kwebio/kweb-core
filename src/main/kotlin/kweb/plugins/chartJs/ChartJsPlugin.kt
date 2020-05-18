package kweb.plugins.chartJs

import kweb.Kweb
import kweb.canvas
import kweb.new
import kweb.plugins.KwebPlugin
import kweb.plugins.chartJs.ChartType.line
import org.jsoup.nodes.Document

class ChartJsPlugin : KwebPlugin() {
    override fun decorate(doc: Document) {
        doc.head().appendElement("script")
                .attr("type", "text/javascript")
                .attr("src", "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.2/Chart.bundle.min.js")
    }
}

val chartJs = ChartJsPlugin()


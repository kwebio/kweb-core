package kweb.plugins.css

import kweb.plugins.KwebPlugin
import kweb.plugins.staticFiles.ResourceFolder
import kweb.plugins.staticFiles.StaticFilesPlugin
import org.jsoup.nodes.Document

/**
 * This Plugin links custom stylesheets in the HTML head tag
 *
 * @property resourceFolder The relative path to the folder in the src/main/resources folder where the .css files are located
 * @property fileNames The list of file names located in the resourceFolder which should be added to the website. Only files with suffix .css (case-insensitive) will be linked
 *
 * @author Cyneath
 */
class CSSPlugin(private val resourceFolder: String, private val fileNames: Set<String>) : KwebPlugin(dependsOn = setOf(StaticFilesPlugin(ResourceFolder(resourceFolder), "/kweb_static/css"))) {
    constructor(resourceFolder: String, fileName: String) : this(resourceFolder, setOf(fileName))

    override fun decorate(doc: Document) {
        fileNames.filter { f -> f.endsWith(".css", true) }.forEach {
            doc.head().appendElement("link")
                .attr("rel", "stylesheet")
                .attr("type", "text/css")
                .attr("href", "/kweb_static/css/$it")
        }
    }
}
package kweb.plugins.javascript

import kweb.plugins.KwebPlugin
import kweb.plugins.staticFiles.ResourceFolder
import kweb.plugins.staticFiles.StaticFilesPlugin
import org.jsoup.nodes.Document

/**
 * Add multiple JavaScript files to your Kweb app from your resources folder.
 *
 * @property resourceFolder The relative path to the folder in the src/main/resources folder where the .js files are located
 * @property fileNames The list of file names located in the resourceFolder which should be added to the website. Only files with suffix .js (case-insensitive) will be linked
 *
 * @author toddharrison
 */
class JavascriptPlugin(private val resourceFolder: String, private val fileNames: Set<String>) : KwebPlugin(dependsOn = setOf(StaticFilesPlugin(ResourceFolder(resourceFolder), "/kweb_static/js"))) {
    constructor(resourceFolder: String, fileName: String) : this(resourceFolder, setOf(fileName))

    override fun decorate(doc: Document) {
        fileNames.filter { f -> f.endsWith(".js", true) }.forEach {
            doc.head().appendElement("script")
                .attr("type", "text/javascript")
                .attr("src", "/kweb_static/js/$it")
        }
    }
}

package kweb.html

import io.ktor.routing.Routing
import kweb.plugins.KwebPlugin
import org.jsoup.nodes.Document
import org.jsoup.nodes.DocumentType
import org.jsoup.nodes.Element
import java.util.*

internal object HtmlDocumentSupplier {
    val appliedPlugins: Set<KwebPlugin> get() = mutableAppliedPlugins

    private val mutableAppliedPlugins: MutableSet<KwebPlugin> = HashSet()
    private lateinit var docTemplate: Document

    fun createDocTemplate(plugins: List<KwebPlugin>, routing: Routing) {
        docTemplate = Document("") // TODO: What should this base URL be?

        docTemplate.appendChild(DocumentType("html", "", ""))

        docTemplate.appendElement("html").let { html: Element ->

            html.appendElement("head").let { head: Element ->

                head.appendElement("meta")
                    .attr("name", "viewport")
                    .attr("content", "width=device-width, initial-scale=1.0")
            }

            html.appendElement("body").let { body: Element ->

                body.attr("onload", "buildPage()")
                body.appendElement("noscript")
                    .html(
                        """
                            | This page is built with <a href="https://kweb.io/">Kweb</a>, which 
                            | requires JavaScript to be enabled.""".trimMargin())
            }
        }

        for (plugin in plugins) {
            // The document will be modified here!
            applyPluginWithDependencies(plugin = plugin, appliedPlugins = mutableAppliedPlugins, document = docTemplate, routeHandler = routing)
        }
    }

    /**
     * The base template for pages
     */
    fun getTemplateCopy() =
        docTemplate.clone()

    private fun applyPluginWithDependencies(
        plugin: KwebPlugin,
        appliedPlugins: MutableSet<KwebPlugin>,
        routeHandler: Routing,
        document: Document
    ) {
        for (dependantPlugin in plugin.dependsOn) {
            if (!appliedPlugins.contains(dependantPlugin)) {
                applyPluginWithDependencies(dependantPlugin, appliedPlugins, routeHandler, document)
                appliedPlugins.add(dependantPlugin)
            }
        }
        if (!appliedPlugins.contains(plugin)) {
            plugin.decorate(document)
            plugin.appServerConfigurator(routeHandler)
            appliedPlugins.add(plugin)
        }
    }
}
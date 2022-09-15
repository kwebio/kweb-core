package kweb.html

import io.ktor.server.routing.*
import kweb.plugins.KwebPlugin
import kweb.state.RenderSpanNames
import org.jsoup.nodes.Document
import org.jsoup.nodes.DocumentType
import org.jsoup.nodes.Element

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
                    .attr("id", "K_head")
                    .attr("name", "viewport")
                    .attr("content", "width=device-width, initial-scale=1.0")

                //these css ids denote spans used in render() and renderEach()
                head.appendElement("style")
                    .html(""".rMStart {display: none;}
                            .${RenderSpanNames.startMarkerClassName} {display: none;}
                            .${RenderSpanNames.endMarkerClassName} {display: none;}
                            .${RenderSpanNames.listStartMarkerClassName} {display: none}
                            .${RenderSpanNames.listEndMarkerClassName} {display: none}
                        """.trimMargin())


                head.appendElement("link")
                    .attr("rel", "stylesheet")
                    .attr("href", "https://cdn.jsdelivr.net/npm/toastify-js/src/toastify.min.css")
            }

            html.appendElement("body").let { body: Element ->

                body.attr("onload", "buildPage()")
                body.attr("id", "K_body")

                body.appendElement("noscript")
                    .html(
                        """
                            | This page is built with <a href="https://kweb.io/">Kweb</a>, which 
                            | requires JavaScript to be enabled.""".trimMargin())
                body.appendElement("script")
                    .attr("src", "https://cdn.jsdelivr.net/npm/toastify-js")
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
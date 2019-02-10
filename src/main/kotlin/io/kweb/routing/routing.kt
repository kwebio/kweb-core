package io.kweb.routing

import io.ktor.routing.*
import io.ktor.routing.RoutingPathSegmentKind.*
import io.kweb.Kweb
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new
import io.kweb.plugins.viewport.ViewportPlugin
import io.kweb.routing.extensions.*
import io.kweb.state.*
import io.kweb.state.persistent.render
import io.mola.galimatias.URL
import mu.KotlinLogging
import kotlin.collections.set

/**
 * @sample testSampleForRouting
 *
 * Created by @@jmdesprez, some modifications by @sanity
 */

// TODO: Handle back button https://www.webdesignerdepot.com/2013/03/how-to-manage-the-back-button-with-javascript/

private val logger = KotlinLogging.logger {}

fun main() {
    testSampleForRouting()
}


fun ElementCreator<*>.route(routeReceiver: RouteReceiver.() -> Unit) {
    val url = this.browser.url(simpleUrlParser)
    val rr = RouteReceiver(this, url)
    routeReceiver(rr)
    val pathKvar = url.path
    val matchingTemplate : KVal<PathTemplate?> = pathKvar.map { path ->
        val size = if (path != listOf("")) path.size else 0
        val templatesOfSameLength = rr.templatesByLength[size]
        templatesOfSameLength?.keys?.firstOrNull { tpl ->
            logger.info("Trying $tpl with $path")
            tpl.isEmpty() || tpl.withIndex().all {
                val tf = it.value.kind != Constant || path[it.index] == it.value.value
                tf
            }
        }
    }

    render(matchingTemplate) { template ->
        if (template != null) {
            val parameters = HashMap<String, KVal<String>>()
            for ((pos, part) in template.withIndex()) {
                if (part.kind == Parameter) {
                    parameters[part.value.substring(1, part.value.length - 1)] = pathKvar[pos]
                }
            }

            val pathRenderer = rr.templatesByLength[template.size]?.get(template)

            if(pathRenderer != null) {
                pathRenderer(this, parameters)
            } else {
                throw RuntimeException("Unable to find pathRenderer for template $template")
            }
        }
    }
}

typealias PathTemplate = List<RoutingPathSegment>
typealias PathReceiver = ElementCreator<*>.(Map<String, KVal<String>>) -> Unit

class RouteReceiver internal constructor(val parentElementCreator: ElementCreator<*>, val url: KVar<URL>) {
    internal val templatesByLength = HashMap<Int, MutableMap<PathTemplate, PathReceiver>>()

    fun path(template : String, pathReceiver : PathReceiver) {
        val routingPath = RoutingPath.parse(template).parts
        templatesByLength.computeIfAbsent(routingPath.size) {HashMap()}[routingPath]= pathReceiver
    }
}

private fun testSampleForRouting() {
    Kweb(port = 1234, plugins = listOf(ViewportPlugin())) {
        doc.body.new {
            route {
                path("/mouse/{mouseid}") { params ->
                    val mouseid = params["mouseid"]
                    h1().text(mouseid!!.map { "Mouse #$it" })
                }
            }
        }
    }
}
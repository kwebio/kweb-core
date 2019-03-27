package io.kweb.routing

import io.ktor.routing.*
import io.ktor.routing.RoutingPathSegmentKind.*
import io.kweb.*
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.events.on
import io.kweb.dom.element.new
import io.kweb.state.*
import io.kweb.state.render.render
import io.mola.galimatias.URL
import mu.KotlinLogging

/**
 * @sample testSampleForRouting
 *
 * Created by @@jmdesprez, some modifications by @sanity
 */

// TODO: Handle back button https://www.webdesignerdepot.com/2013/03/how-to-manage-the-back-button-with-javascript/

private val logger = KotlinLogging.logger {}

fun main() {
    test3()
}


fun ElementCreator<*>.route(cacheOnClient : Boolean = false, routeReceiver: RouteReceiver.() -> Unit) {
    val url = this.browser.url(simpleUrlParser)
    val rr = RouteReceiver(this, url)
    routeReceiver(rr)
    val pathKvar = url.pathSegments
    val matchingTemplate : KVal<PathTemplate?> = pathKvar.map { path ->
        val size = if (path != listOf("")) path.size else 0
        val templatesOfSameLength = rr.templatesByLength[size]
        val tpl = templatesOfSameLength?.keys?.firstOrNull { tpl ->
            tpl.isEmpty() || tpl.withIndex().all {
                val tf = it.value.kind != Constant || path[it.index] == it.value.value
                tf
            }
        }
        tpl
    }

    render(matchingTemplate, cacheOnClient = cacheOnClient) { template ->
        if (template != null) {
            val parameters = HashMap<String, KVar<String>>()
            for ((pos, part) in template.withIndex()) {
                if (part.kind == Parameter) {
                    val str = part.value
                    parameters[str.substring(str.indexOf('{')+1, str.indexOf('}'))] = pathKvar[pos]
                }
            }

            val pathRenderer = rr.templatesByLength[template.size]?.get(template)

            if(pathRenderer != null) {
                pathRenderer(this, parameters)
            } else {
                throw RuntimeException("Unable to find pathRenderer for template $template")
            }
        } else {
            throw NotFoundException("Page not found")
        }
    }
}

typealias PathTemplate = List<RoutingPathSegment>
typealias PathReceiver = ElementCreator<*>.(params : Map<String, KVar<String>>) -> Unit

class RouteReceiver internal constructor(val parentElementCreator: ElementCreator<*>, val url: KVar<URL>) {
    internal val templatesByLength = HashMap<Int, MutableMap<PathTemplate, PathReceiver>>()

    fun path(template : String, pathReceiver : PathReceiver) {
        val routingPath = RoutingPath.parse(template).parts
        templatesByLength.computeIfAbsent(routingPath.size) {HashMap()}[routingPath]= pathReceiver
    }
}

private fun testSampleForRouting() {
    Kweb(port = 16097) {
        doc.body.new {
            route {
                path("/users/{userId}") { params ->
                    val userId = params.getValue("userId")
                    h1().text(userId.map { "User id: $it" })
                }
                path("/lists/{listId}") { params ->
                    val listId = params.getValue("listId")
                    h1().text(listId.map { "List id: $it" })
                }
            }
        }
    }
}

private fun test2() {
    Kweb(port = 16097) {
        doc.body.new {
            val path = url(simpleUrlParser).path
            route {
                path("/") {
                    path.value = "/number/1"
                }
                path("/number/{num}") { params ->
                    val num = params.getValue("num").toInt()
                    a().text(num.map {"Number $it"}).on.click {
                        path.value = "/number/${num.value + 1}"
                    }
                }
            }
        }
    }
}

private fun test3() {
    Kweb(port = 16097) {
        doc.body.new {
            route {
                path("/") {
                    h1().text("Hello World!")

                    button().text("Home Page").on.click {
                        url.path.value = "/"
                    }

                    button().text("Test Page").on.click {
                        url.path.value = "/test"
                    }
                }

                path("/test") { params ->
                    button().text("Home Page").on.click {
                        url.path.value = "/"
                    }

                    button().text("Test Page 2").on.click {
                        url.path.value = "/test2"
                    }
                }

                path("/test2") { params ->
                    h1().text("Test Page 2!")

                    button().text("Home Page").on.click {
                        url.path.value = "/"
                    }
                }
            }
        }
    }
}
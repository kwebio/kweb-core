package kweb.routing

import io.ktor.routing.RoutingPath
import io.ktor.routing.RoutingPathSegment
import io.ktor.routing.RoutingPathSegmentKind.Constant
import io.ktor.routing.RoutingPathSegmentKind.Parameter
import kweb.Kweb
import kweb.dom.element.creation.ElementCreator
import kweb.dom.element.creation.tags.h1
import kweb.dom.element.new
import kweb.state.*
import kweb.state.render.closeOnElementCreatorCleanup
import kweb.state.render.render
import io.mola.galimatias.URL
import mu.KotlinLogging
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.all
import kotlin.collections.firstOrNull
import kotlin.collections.getValue
import kotlin.collections.listOf
import kotlin.collections.set
import kotlin.collections.withIndex

/**
 * @sample testSampleForRouting
 */

// TODO: Handle back button https://www.webdesignerdepot.com/2013/03/how-to-manage-the-back-button-with-javascript/

private val logger = KotlinLogging.logger {}

fun ElementCreator<*>.route(routeReceiver: RouteReceiver.() -> Unit) {
    val rr = RouteReceiver()
    routeReceiver(rr)
    val pathKVar : KVar<List<String>> = this.browser.url.map( object : ReversibleFunction<String, List<String>>(label = "url -> pathSegments") {
        override fun invoke(from: String): List<String> {
            return URL.parse(from).pathSegments()
        }

        override fun reverse(original: String, change: List<String>): String {
            return URL.parse(original).withPath(change.joinToString(separator = "/")).toString()
        }
    } )
    val matchingTemplate : KVal<PathTemplate?> = pathKVar.map { path ->
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

    render(matchingTemplate) { template ->
        if (template != null) {
            val parameters = HashMap<String, KVar<String>>()
            for ((pos, part) in template.withIndex()) {
                if (part.kind == Parameter) {
                    val str = part.value
                    val paramKVar = pathKVar[pos]
                    closeOnElementCreatorCleanup(paramKVar)
                    parameters[str.substring(str.indexOf('{')+1, str.indexOf('}'))] = paramKVar
                }
            }

            val pathRenderer = rr.templatesByLength[template.size]?.get(template)

            if(pathRenderer != null) {
                pathRenderer(this, parameters)
            } else {
                error("Unable to find pathRenderer for template $template")
            }
        } else {
            rr.notFoundReceiver.invoke(this, URL.parse(this.browser.url.value).path() )
        }
    }
}


typealias PathTemplate = List<RoutingPathSegment>
typealias PathReceiver = ElementCreator<*>.(params : Map<String, KVar<String>>) -> Unit
typealias NotFoundReceiver = (ElementCreator<*>).(path : String) -> Unit

class RouteReceiver internal constructor() {
    internal val templatesByLength = HashMap<Int, MutableMap<PathTemplate, PathReceiver>>()

    internal var notFoundReceiver : NotFoundReceiver = { path ->
        h1().text("Not Found: $path")
    }

    fun path(template : String, pathReceiver : PathReceiver) {
        val routingPath = RoutingPath.parse(template).parts
        templatesByLength.computeIfAbsent(routingPath.size) {HashMap()}[routingPath]= pathReceiver
    }

    fun notFound(receiver : NotFoundReceiver) {
        notFoundReceiver = receiver
    }
}

private fun testSampleForRouting() {
    Kweb(port = 16097, buildPage = {
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
    })
}

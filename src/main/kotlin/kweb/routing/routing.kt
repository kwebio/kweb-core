package kweb.routing

import io.ktor.routing.RoutingPathSegment
import io.ktor.routing.RoutingPathSegmentKind.Constant
import io.ktor.routing.RoutingPathSegmentKind.Parameter
import io.mola.galimatias.URL
import kweb.ElementCreator
import kweb.Kweb
import kweb.h1
import kweb.new
import kweb.state.*
import mu.KotlinLogging
import kotlin.collections.set

/**
 * @sample testSampleForRouting
 */

// TODO: Handle back button https://www.webdesignerdepot.com/2013/03/how-to-manage-the-back-button-with-javascript/

private val logger = KotlinLogging.logger {}

fun ElementCreator<*>.route(routeReceiver: RouteReceiver.() -> Unit) {
    val rr = RouteReceiver()
    routeReceiver(rr)
    val pathKVar : KVar<List<String>> = this.browser.url.map(UrlToPathSegmentsRF)
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

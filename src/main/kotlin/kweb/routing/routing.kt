package kweb.routing

import io.ktor.server.routing.*
import kweb.ElementCreator
import kweb.Kweb
import kweb.h1
import kweb.route
import kweb.state.KVar

/**
 * // @sample testSampleForRouting
 */

// TODO: Handle back button https://www.webdesignerdepot.com/2013/03/how-to-manage-the-back-button-with-javascript/

typealias PathTemplate = List<RoutingPathSegment>
typealias PathReceiver = ElementCreator<*>.(params: Map<String, KVar<String>>) -> Unit
typealias NotFoundReceiver = (ElementCreator<*>).(path: String) -> Unit

private fun testSampleForRouting() {
    Kweb(port = 16097, buildPage = {
        doc.body {
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

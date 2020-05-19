package kweb.routing

import io.ktor.routing.RoutingPathSegment
import kweb.*
import kweb.state.KVar

/**
 * @sample testSampleForRouting
 */

// TODO: Handle back button https://www.webdesignerdepot.com/2013/03/how-to-manage-the-back-button-with-javascript/

typealias PathTemplate = List<RoutingPathSegment>
typealias PathReceiver = ElementCreator<*>.(params: Map<String, KVar<String>>) -> Unit
typealias NotFoundReceiver = (ElementCreator<*>).(path: String) -> Unit

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

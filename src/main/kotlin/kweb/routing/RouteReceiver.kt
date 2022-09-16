package kweb.routing

import io.ktor.server.routing.RoutingPath
import kweb.h1

class RouteReceiver internal constructor() {
    internal val templatesByLength = HashMap<Int, MutableMap<PathTemplate, PathReceiver>>()

    internal var notFoundReceiver: NotFoundReceiver = { path ->
        h1().text("Not Found: $path")
    }

    fun path(template: String, pathReceiver: PathReceiver) {
        val routingPath = RoutingPath.parse(template).parts
        templatesByLength.computeIfAbsent(routingPath.size) { HashMap() }[routingPath] = pathReceiver
    }

    fun notFound(receiver: NotFoundReceiver) {
        notFoundReceiver = receiver
    }
}
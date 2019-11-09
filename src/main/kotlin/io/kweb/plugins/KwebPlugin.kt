package io.kweb.plugins

import io.ktor.routing.Routing
import io.kweb.dom.element.Element
import java.util.*

abstract class KwebPlugin(val dependsOn: Set<KwebPlugin> = Collections.emptySet()) {
    // TODO: Allow plugins to specify any other plugin that they should
    // TODO: run before or after, so that the user doesn't need to
    // TODO: worry about getting the order right.

    abstract fun decorate(startHead: StringBuilder, endHead: StringBuilder)

    open fun executeAfterPageCreation() = ""

    open fun appServerConfigurator(routeHandler : Routing) {

    }

    open fun elementCreationHook(element : Element) {

    }
}
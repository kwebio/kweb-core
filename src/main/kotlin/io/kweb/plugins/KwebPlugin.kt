package io.kweb.plugins

import io.ktor.routing.Routing
import io.kweb.dom.element.Element
import org.jsoup.nodes.Document
import java.util.*

abstract class KwebPlugin(val dependsOn: Set<KwebPlugin> = Collections.emptySet()) {

    open fun decorate(doc : Document) {

    }

    open fun executeAfterPageCreation() = ""

    open fun appServerConfigurator(routeHandler : Routing) {

    }

    open fun elementCreationHook(element : Element) {

    }
}
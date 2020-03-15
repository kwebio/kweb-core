package io.kweb.plugins

import io.ktor.routing.Routing
import io.kweb.dom.element.Element
import java.util.*
import org.jsoup.nodes.Document as JSoupDocument

abstract class KwebPlugin(val dependsOn: Set<KwebPlugin> = Collections.emptySet()) {

    /**
     * Override this to make changes to the initial HTML document
     */
    open fun decorate(doc : JSoupDocument) {

    }

    /**
     * Override this to provide JavaScript to be executed after page creation
     */
    open fun executeAfterPageCreation() = ""

    /**
     * Override this to add routes via KTor
     */
    open fun appServerConfigurator(routeHandler : Routing) {

    }

    /**
     * Override this to execute code when a new element is created
     */
    open fun elementCreationHook(element : Element) {

    }
}
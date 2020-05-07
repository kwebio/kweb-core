package kweb

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.routing.get
import io.ktor.routing.routing

/**
 * Kweb normally calls this function on initialization, if you provide a buildpage,
 * regardless of whether it's initialized as a standalone, or Ktor Feature.
 *
 * If you are using Ktor, this is an easy way to migrate from the deprecated buildPage design
 *
 * Kweb will still do its internal routing properly, e.g if you use `path()` in your buildPage,
 * but it will also respect Ktor defined routes ^-^
 */
fun Application.installKwebOnRemainingRoutes(buildPage: WebBrowser.() -> Unit) {
    routing {
        get("/{visitedUrl...}") {
            call.respondKweb(buildPage)
        }
    }
}

/**
 * Allows for defining Kweb responses inlined in Ktor routing code
 *
 * @see kweb.demos.feature.kwebFeature
 */
suspend fun ApplicationCall.respondKweb(buildPage: WebBrowser.() -> Unit) =
    application.feature(Kweb).respondKweb(this, buildPage)

/**
 * Use this if you are on Ktor and migrating out of the deprecated buildPage design
 */
const val buildPageReplacementCode = """
    routing {
        get("/{visitedUrl...}") {
            call.respondKweb { 
                buildPage
            }
        }
    }
    """
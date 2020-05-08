package kweb

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.routing.get
import io.ktor.routing.routing
import kweb.routing.PathReceiver

/**
 * Kweb normally calls this function on initialization, if you provide a buildpage,
 * regardless of whether it's initialized as a standalone, or Ktor Feature.
 *
 * If you are using Ktor, this is an easy way to migrate from the deprecated buildPage design
 *
 * Kweb will still do its internal routing properly, e.g if you use `path()` in your buildPage. ^-^
 *
 * Nevertheless, please be very careful and avoid using both models in parallel as they
 * have lots of subtle differences and you're bound to hit something!
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
 * If you were previously using Kweb routes and you want to switch to Ktor, this is for you.
 * You can copy your previous path receivers and they will be run the same way as before,
 *
 * EXCEPT FOR THE PARAMETERS
 *
 * Note that parameter handling is no longer done by Kweb, you'll have to use the Ktor
 * [io.ktor.http.Parameters] functions
 *
 * @param pathReceiver the receiver to execute on this path
 */
suspend fun ApplicationCall.respondKwebRender(pathReceiver: PathReceiver) =
    respondKweb {
        doc.body.new { span().new { pathReceiver(this, emptyMap()) } }
    }

/**
 * Use this if you are on Ktor and migrating out of the deprecated buildPage design
 */
const val buildPageReplacementCode = """
    routing {
        get("/{visitedUrl...}") {
            call.respondKwebRender { 
                buildPage
            }
        }
    }
    """
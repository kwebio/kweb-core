package kweb.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kweb.Kweb
import kweb.h1

/**
 * A Kweb plugin that adds a favicon.ico route to the Ktor server. Note that
 * the [Kweb] constructor will automatically add FaviconPlugin.notFound() to
 * the list of plugins if no favicon plugin is provided.
 */
class FaviconPlugin(private val response : suspend (ApplicationCall).() -> Unit) : KwebPlugin() {
    companion object {
        /**
         * A convenience constructor that responds with a 404
         */
        fun notFound() = FaviconPlugin {
            respond(HttpStatusCode.NotFound, "favicon.ico not found")
        }
    }

    override fun appServerConfigurator(routeHandler: Routing) {
        routeHandler.get("/favicon.ico") {
            response(this.call)
        }
    }
}

private fun faviconExample() {
// ANCHOR: favicon
val faviconPlugin = FaviconPlugin {
    respondBytes(getFaviconAsBytes(), ContentType.Image.Any)
}
Kweb(port = 16097, plugins = listOf(faviconPlugin)) {
    doc.body {
        // ...
    }
}
// ANCHOR_END: favicon
}

private fun getFaviconAsBytes() : ByteArray {
    TODO("Load from a resource or file")
}

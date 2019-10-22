package io.kweb.plugins.staticFiles

import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.routing.Routing
import io.kweb.plugins.KwebPlugin
import java.io.File

/**
 * @author rpanic
 *
 * This Plugin serves static files to be used in the frontend
 *
 * @property rootFolder The root folder, where the static assets are saved
 * @property servedRoute The route where these assets are being served
 */
class StaticFilesPlugin(private val rootFolder: File, private val servedRoute: String = "assets") : KwebPlugin(){

    override fun decorate(startHead: StringBuilder, endHead: StringBuilder){}

    override fun appServerConfigurator(routeHandler: Routing) {
        routeHandler.static(servedRoute){
            staticRootFolder = rootFolder
            files(".")
        }
    }

}

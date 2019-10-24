package io.kweb.plugins.staticFiles

import io.ktor.http.content.files
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.kweb.plugins.KwebPlugin
import io.kweb.plugins.jqueryCore.jqueryCore
import java.io.File
import java.net.URL

/**
 * @author rpanic
 *
 * This Plugin serves static files to be used in the frontend
 *
 * @property rootFolder The root folder, where the static assets are saved
 * @property resourceFolder For serving resources, the path to the folder which will be served
 * @property servedRoute The route where these assets are being served
 */
class StaticFilesPlugin private constructor(private val servedRoute: String = "assets") : KwebPlugin() {

    private lateinit var datasource: (Route) -> Unit

    constructor(rootFolder: File, servedRoute: String = "assets") : this(servedRoute) {
        datasource = {
            it.staticRootFolder = rootFolder
        }
    }

    constructor(resourceFolder: ResourceFolder, servedRoute: String = "assets") : this(servedRoute) {
        datasource = {
            it.resources(resourceFolder.resourceFolder)
        }
    }

    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {}

    override fun appServerConfigurator(routeHandler: Routing) {
        routeHandler.static(servedRoute) {
            datasource(this)
            files(".")
        }
    }

}

internal const val kwebInternalStaticFilePath = "kweb_native_assets"
internal val kwebInternalStaticFilesPlugin = StaticFilesPlugin(ResourceFolder("io/kweb/assets"), servedRoute = kwebInternalStaticFilePath)

data class ResourceFolder(val resourceFolder: String)
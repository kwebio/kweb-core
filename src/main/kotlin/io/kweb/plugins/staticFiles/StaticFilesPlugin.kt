package io.kweb.plugins.staticFiles

import io.ktor.http.content.*
import io.ktor.routing.*
import io.kweb.plugins.KwebPlugin
import java.io.File

/**
 * @author rpanic
 *
 * This Plugin serves static files to be used in the frontend
 *
 * @property rootFolder The root folder, where the static assets are saved
 * @property resourceFolder For serving resources, the path to the folder which will be served
 * @property servedRoute The route where these assets are being served
 */
class StaticFilesPlugin private constructor(private val servedRoute: String) : KwebPlugin() {

    private lateinit var datasource: (Route) -> Unit

    constructor(rootFolder: File, servedRoute: String) : this(servedRoute) {
        datasource = {
            it.staticRootFolder = rootFolder
        }
    }

    constructor(resourceFolder: ResourceFolder, servedRoute: String) : this(servedRoute) {
        datasource = {
            it.resources(resourceFolder.resourceFolder)
        }
    }

    override fun appServerConfigurator(routeHandler: Routing) {
        routeHandler.static(servedRoute) {
            datasource(this)
            files(".")
        }
    }

}

/* TODO: This probably needs a rethink, it should be possible to interrogate a [StaticFilePlugin]
   TODO: to determine the static file path, we shouldn't be hardwiring it like this.
*/
internal const val internalStaticFilePath = "/static"

data class ResourceFolder(val resourceFolder: String)
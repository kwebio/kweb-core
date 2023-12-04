package kweb.plugins.image

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kweb.plugins.KwebPlugin

/**
 * Add multiple dynamically generated images files to your Kweb app routing using ByteArray generator functions.
 *
 * @property resourceFolder The relative logical path used for routing to the dynamic images.
 * @property fileNames The map of file names to ByteArray providers, called once every time the image is requested.
 *
 * @author toddharrison
 */
class DynamicImagePlugin(
    private val resourceFolder: String,
    private val fileNames: Map<String, suspend (Parameters) -> ByteArray>,
): KwebPlugin() {
    constructor(resourceFolder: String, fileName: String, byteProvider: suspend (Parameters) -> ByteArray):
            this(resourceFolder, mapOf(fileName to byteProvider))

    override fun appServerConfigurator(routeHandler: Routing) {
        fileNames.forEach { (fileName, byteProvider) ->
            routeHandler.get("$resourceFolder/$fileName") { _ ->
                context.respondBytes {
                    byteProvider(context.parameters)
                }
            }
        }
    }
}

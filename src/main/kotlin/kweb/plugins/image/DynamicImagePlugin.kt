package kweb.plugins.image

import io.ktor.server.response.*
import io.ktor.server.routing.*
import kweb.plugins.KwebPlugin
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

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
    private val fileNames: Map<String,
    suspend () -> ByteArray>
): KwebPlugin() {
    constructor(resourceFolder: String, fileName: String, byteProvider: suspend () -> ByteArray):
            this(resourceFolder, mapOf(fileName to byteProvider))

    constructor(resourceFolder: String, fileNames: Map<String, suspend () -> BufferedImage>, format: String):
            this(resourceFolder, fileNames
                .mapValues { (_, value) -> suspend { toByteArray(value(), format) }})

    constructor(resourceFolder: String, fileName: String, bufferedImageProvider: suspend () -> BufferedImage, format: String):
            this(resourceFolder, mapOf(fileName to bufferedImageProvider), format)

    override fun appServerConfigurator(routeHandler: Routing) {
        fileNames.forEach { (fileName, byteProvider) ->
            routeHandler.get("$resourceFolder/$fileName") { _ ->
                context.respondBytes {
                    byteProvider()
                }
            }
        }
    }
}

fun toByteArray(bufferedImage: BufferedImage, format: String): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    ImageIO.write(bufferedImage, format, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

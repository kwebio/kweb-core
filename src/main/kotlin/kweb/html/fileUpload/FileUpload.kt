package kweb.html.fileUpload

import kotlinx.serialization.Serializable

@Serializable
class FileUpload(
        val fileName: String,
        val fileSize: String,
        val base64Content: String
)
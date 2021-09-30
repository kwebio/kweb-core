package kweb.html.fileUpload

import kotlinx.serialization.Serializable

@Serializable
class FileUpload(
        val fileName: String,
        val fileSize: Int,
        val base64Content: String
)
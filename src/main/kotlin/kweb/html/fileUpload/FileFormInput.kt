package kweb.html.fileUpload


import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kweb.Element
import kweb.util.random
import mu.two.KotlinLogging
import kotlin.math.abs

private val logger = KotlinLogging.logger {}

class FileFormInput {

    private lateinit var _inputElement: Element

    val inputElement: Element
        get() = _inputElement


    fun setInputElement(e: Element) {
        this._inputElement = e
    }

    fun setAccept(acceptedTypes: String): Unit {
        inputElement.browser.callJsFunction(
            """document.getElementById({}).accept = {};""",
            JsonPrimitive(inputElement.id),
            JsonPrimitive(acceptedTypes)
        )
    }

    fun isMultiSelect(isMultiple: Boolean): Unit {
        inputElement.browser.callJsFunction(
            "document.getElementById({}).multiple = {}", JsonPrimitive(inputElement.id), JsonPrimitive(isMultiple)
        )
    }

    fun onFileSelect(onFileSelectCallback: () -> Unit) {
        inputElement.on.change { _ ->
            onFileSelectCallback()
        }
    }

    fun retrieveFile(onFileRetrieveCallback: (FileUpload) -> Unit) {
        val callbackId = abs(random.nextInt())

        val js = """
                let fd = document.getElementById({}).files[0]
                let fr = new FileReader()
                fr.readAsDataURL(fd)
                fr.onload = function(){
                    callbackWs({}, {base64Content: fr.result, fileSize: fd.size, fileName: fd.name});
                }
            """

        inputElement.browser.callJsFunctionWithCallback(js, callbackId, callback = { result ->
            logger.info("Result is $result")
            onFileRetrieveCallback(Json.decodeFromString(FileUpload.serializer(), result.toString()))
        }, JsonPrimitive(inputElement.id), JsonPrimitive(callbackId))
        inputElement.creator?.onCleanup(true) {
            inputElement.browser.removeCallback(callbackId)
        }
    }
}

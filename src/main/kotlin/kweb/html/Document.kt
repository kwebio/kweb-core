package kweb.html

import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kweb.*
import kweb.html.events.Event
import kweb.html.events.EventGenerator
import kweb.html.events.OnImmediateReceiver
import kweb.html.events.OnReceiver
import kweb.util.random
import kotlin.math.abs

/**
 * Represents the in-browser Document Object Model, corresponding to the JavaScript
 * [document](https://www.w3schools.com/jsref/dom_obj_document.asp) object.
 *
 * Passed in as `doc` to the `buildPage` lambda of the [Kweb] constructor.
 */
class Document(val receiver: WebBrowser) : EventGenerator<Document> {

    private val documentScope = CoroutineScope(Dispatchers.IO)

    fun getElementById(id: String) = Element(receiver, null, "return document.getElementById(\"$id\")", id = id)

    val cookie = CookieReceiver(receiver)

    val body = BodyElement(receiver, "K_body")

    fun body(new: (ElementCreator<BodyElement>.(BodyElement) -> Unit)? = null) : BodyElement {
        if (new != null) {
            val ec = ElementCreator(element = body, insertBefore = null)
            new(ec, body)
            receiver.addCloseListener {
                ec.cleanup()
            }
        }
        return body
    }

    val head = HeadElement(receiver, "K_head")

    fun head(new: (ElementCreator<HeadElement>.(HeadElement) -> Unit)? = null) : HeadElement {
        if (new != null) {
            val ec = ElementCreator(element = head, insertBefore = null)
            new(ec, head)
            receiver.addCloseListener {
                ec.cleanup()
            }
        }
        return head
    }

    suspend fun getOrigin(): Any {
        return receiver.callJsFunctionWithResult("return document.origin")
    }

    fun execCommand(command: String) {
        //language=JavaScript
        receiver.callJsFunction("document.execCommand({});", JsonPrimitive(command))
    }

    /**
     * Allows data to be stored in and retrieved from the browser's [local storage](https://www.w3schools.com/html/html5_webstorage.as).
     */
    val localStorage get() = StorageReceiver(receiver, StorageType.local)


    /**
     * Allows data to be stored in and retrieved from the browser's [session storage](https://www.w3schools.com/html/html5_webstorage.as).
     */
    val sessionStorage get() = StorageReceiver(receiver, StorageType.session)

    override val browser = receiver

    override fun addImmediateEventCode(eventName: String, jsCode: String) {
        val wrappedJS = """
            return document.addEventListener({}, function(event) {
                $jsCode
            });
        """.trimIndent()
        documentScope.launch {
            receiver.callJsFunctionWithResult(wrappedJS, JsonPrimitive(eventName))
        }
    }


    override fun addEventListener(eventName: String, returnEventFields: Set<String>, retrieveJs: String?, preventDefault : Boolean, callback: (JsonElement) -> Unit): Document {
        val callbackId = abs(random.nextInt())
        val retrieveJs = if (retrieveJs != null) ", \"retrieved\" : ($retrieveJs)" else ""
        val eventObject = "{" + returnEventFields.joinToString(separator = ", ") { "\"$it\" : event.$it" } + retrieveJs + "}"
        val js = """
            document.addEventListener({}, function(event) {
                ${if (preventDefault) "event.preventDefault();" else ""}
                callbackWs({}, $eventObject);
            });
        """
        receiver.callJsFunctionWithCallback(js, callbackId, callback = { payload ->
            callback.invoke(payload)
        }, JsonPrimitive(eventName), JsonPrimitive(callbackId))
        return this
    }

    /**
     * See [here](https://docs.kweb.io/en/latest/dom.html#listening-for-events).
     */
    val on: OnReceiver<Document> get() = OnReceiver(this, preventDefault = false)

    /**
     * You can supply a javascript expression `retrieveJs` which will
     * be available via [Event.retrieveJs]
     */
    fun on(retrieveJs: String? = null, preventDefault: Boolean = false) = OnReceiver(this, retrieveJs, preventDefault)

    /**
     * See [here](https://docs.kweb.io/en/latest/dom.html#immediate-events).
     */
    val onImmediate get() = OnImmediateReceiver(this)

}

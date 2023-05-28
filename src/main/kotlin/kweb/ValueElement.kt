package kweb

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kweb.ValueElement.LastModificationSource.Browser
import kweb.ValueElement.LastModificationSource.Server
import kweb.html.events.Event
import kweb.state.CloseReason
import kweb.state.KVal
import kweb.state.KVar
import kweb.state.ReversibleFunction
import kweb.util.json

/**
 * Abstract class for the various elements that have a `value` attribute and which support `change` and `input` events.
 *
 * @param kvarUpdateEvent The [value] of this element will update on this event, defaults to [input](https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/input_event)
 */
abstract class ValueElement(
    open val element: Element, val kvarUpdateEvent: String = "input",
    val initialValue: String? = null
) : Element(element) {
    val valueJsExpression: String by lazy { "document.getElementById(\"$id\").value" }

    suspend fun getValue(): String {
        return when (val result =
            element.browser.callJsFunctionWithResult("return document.getElementById({}).value;", id.json)) {
            is JsonPrimitive -> result.content
            else -> error("Needs to be JsonPrimitive")
        }
    }

    //language=JavaScript
    fun setValue(newValue: String) {
        element.browser.callJsFunction(
            """
                const element = document.getElementById({});
                element.value = {};
                delete element.dataset.previousInput;
                """,
            element.id.json, newValue.json
        )
    }

    fun setValue(newValue: KVal<String>) {
        val initialValue = newValue.value
        setValue(initialValue)
        val listenerHandle = newValue.addListener { _, new ->
            setValue(new)
        }
        element.creator?.onCleanup(true) {
            newValue.removeListener(listenerHandle)
        }
    }

    data class Value(val value: String, val lastModificationSource: LastModificationSource)
    enum class LastModificationSource {
        Server, Browser
    }

    private var _valueKvar: KVar<Value>? = null

    private lateinit var _stringValueKvar: KVar<String>


    /**
     * A KVar bidirectionally synchronized with the [value of a select element](https://www.w3schools.com/jsref/prop_select_value.asp).
     * This [KVar] will update if the select element is changed (depending on [kvarUpdateEvent]), and will modify
     * the element value if the KVar is changed.
     *
     * [value] can be set to a `KVar<String>` to synchronize with an existing KVar, or it will create a new `KVar("")`
     * if not set.
     */
    var value: KVar<String>
        get() {
            if (_valueKvar == null) {
                synchronized(this) {
                    _valueKvar = KVar(Value(initialValue ?: "", Server))
                    _stringValueKvar =
                        _valueKvar!!.map(object : ReversibleFunction<Value, String>("ValueElement.value") {
                            override fun invoke(from: Value): String = from.value

                            override fun reverse(original: Value, change: String): Value =
                                Value(change, Server)

                        })
                    this.creator?.onCleanup(true) {
                        value.close(CloseReason("Parent element closed"))
                    }
                    attachListeners(_valueKvar!!)
                    updateKVar(_valueKvar!!, updateOn = kvarUpdateEvent)
                }
            }
            return _stringValueKvar
        }
        set(v) {
            if (_valueKvar != null) error("`value` may only be set once, and cannot be set after it has been retrieved")
            synchronized(this) {
                setValue(v.value)
                _stringValueKvar = v
                _valueKvar = _stringValueKvar.map(object : ReversibleFunction<String, Value>("ValueElement.value") {
                    override fun invoke(from: String): Value = Value(from, Server)

                    override fun reverse(original: String, change: Value): String = change.value

                })
                attachListeners(_valueKvar!!)
                updateKVar(_valueKvar!!, updateOn = kvarUpdateEvent)
            }
        }

    private fun attachListeners(kv: KVar<Value>) {
        val handle = kv.addListener { _, value ->
            // Only update the DOM element if the source of the change was the server
            if (value.lastModificationSource == Server) {
                setValue(value.value)
            }
        }
        element.creator?.onCleanup(true) {
            kv.removeListener(handle)
        }
    }

    /**
     * Automatically update `toBind` with the value of this INPUT element when `updateOn` event occurs.
     */

    @Serializable
    data class DiffData(val prefixEndIndex: Int, val postfixOffset: Int, val diffString: String)

    private fun applyDiff(oldString: String, diffData: DiffData): String {

        val newString = when {
            diffData.postfixOffset == -1 -> {//these 2 edge cases prevent the prefix or the postfix from being
                // repeated when you append text to the beginning of the text or the end of the text
                oldString.substring(0, diffData.prefixEndIndex) + diffData.diffString
            }

            diffData.prefixEndIndex == 0 -> {
                diffData.diffString + oldString.substring(oldString.length - diffData.postfixOffset)
            }

            else -> {
                oldString.substring(0, diffData.prefixEndIndex) + diffData.diffString +
                        oldString.substring(oldString.length - diffData.postfixOffset)
            }
        }
        return newString
    }

    private fun updateKVar(toBind: KVar<Value>, updateOn: String = "input") {
        on(
            //language=JavaScript
            retrieveJs = "get_diff_changes(document.getElementById(\"${element.id}\"))"
        )
            .event<Event>(updateOn) {
                //TODO, this check shouldn't be necessary. It should be impossible for get_diff_changes() to return a null,
                //but we had a null check previously, so I went ahead and added it.
                if (it.retrieved != JsonNull) {
                    val diffDataJson = it.retrieved
                    val diffData = Json.decodeFromJsonElement(DiffData.serializer(), diffDataJson)
                    toBind.value = Value(applyDiff(toBind.value.value, diffData), Browser)
                }
            }
    }

}
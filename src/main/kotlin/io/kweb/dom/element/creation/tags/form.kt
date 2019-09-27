package io.kweb.dom.element.creation.tags

import com.github.salomonbrys.kotson.toJson
import io.kweb.dom.attributes.*
import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.events.ONReceiver
import io.kweb.dom.element.events.on
import io.kweb.dom.element.read.ElementReader
import io.kweb.state.KVal
import io.kweb.state.KVar
import java.util.concurrent.CompletableFuture

/**
 *
 */

fun ElementCreator<Element>.input(type: InputType? = null, name: String? = null, initialValue: String? = null, size: Int? = null, placeholder: String? = null, attributes: Map<String, Any> = attr): InputElement {
    return InputElement(element("input", attributes = attributes
            .set("type", type?.name)
            .set("name", name)
            .set("value", initialValue)
            .set("placeholder", placeholder)
            .set("size", size)
    ))
}

// TODO: Other element types might also benefit from some of this functionality, extract a common parent Element type
open class InputElement(override val element: Element) :ValueElement(element) {
    fun checked(checked: Boolean) = setAttribute("checked", checked)


    fun select() = element.execute("$jsExpression.select();")

    fun setReadOnly(ro: Boolean) = element.execute("$jsExpression.readOnly = $ro;")
}

enum class InputType {
    button, checkbox, color, date, datetime, email, file, hidden, image, month, number, password, radio, range, reset, search, submit, tel, text, time, url, week
}

fun ElementCreator<Element>.textarea(rows : Int? = null, cols : Int? = null, required : Boolean? = null, attributes: Map<String, Any> = attr) : TextAreaElement {
    return TextAreaElement(element("textarea", attributes = attributes
            .set("rows", rows)
            .set("cols", cols)
            .set("required", required)
    ))
}

open class SelectElement(parent: Element) : ValueElement(parent, kvarUpdateEvent = "change")
fun <T : Any> ElementCreator<Element>.select(options: List<T>, optionValue: (T) -> String, optionText: (T) -> String = { it.toString() }, name: String? = null, required : Boolean? = null, attributes: Map<String, Any> = attr) : SelectElement {
    return select(options.map {
        optionValue(it) to optionText(it)
    }, name, required, attributes)
}
fun ElementCreator<Element>.select(options: List<Pair<String, Any>>, name: String? = null, required : Boolean? = null, attributes: Map<String, Any> = attr) : SelectElement {
    val selectElement = element("select", attributes = attributes
            .set("name", name)
            .set("required", required)
    )

    ElementCreator(selectElement).apply {
        for (i in options) {
            element("option", mapOf("value" to i.first)).text(i.second.toString())
        }
    }

    return SelectElement(selectElement)
}

open class TextAreaElement(parent: Element) : ValueElement(parent) {
    override val read get() = TextAreaElementReader(this)
}
open class TextAreaElementReader(element : TextAreaElement) : ElementReader(element) {
    val value get() = receiver.evaluate("($jsExpression.innerText);")
}


fun ElementCreator<Element>.label(forId: String?, attributes: Map<String, Any> = attr) = LabelElement(element("label", attributes = attributes.set("for", forId)))

fun ElementCreator<Element>.label(for_: Element? = null, attributes: Map<String, Any> = attr) = LabelElement(element("label", attributes = attributes.set("for", for_?.id)))

open class LabelElement(wrapped: Element) : Element(wrapped)

/**
 * Abstract class for the various elements that have a `value` attribute and which support `change` and `input` events.
 */
abstract class ValueElement(open val element : Element, val kvarUpdateEvent : String = "input") : Element(element) {
    fun getValue(): CompletableFuture<String> = element.evaluate("$jsExpression.value;") { s: String -> s }
            ?: throw RuntimeException("Not sure why .evaluate() would return null")

    fun setValue(newValue: String) = element.browser.execute("$jsExpression.value=${newValue.toJson()};")
    fun setValue(newValue: KVal<String>) {
        val initialValue = newValue.value
        setValue(initialValue)
        newValue.addListener { _, new ->
            setValue(new)
        }
    }

    private @Volatile var _valueKvar : KVar<String>? = null

    var value : KVar<String> get() {
        if (_valueKvar == null) {
            _valueKvar = KVar("")
        }
        return _valueKvar!!
    }
        set(v) {
            if (_valueKvar != null) throw RuntimeException("`value` may only be set once, and cannot be set after it has been retrieved")
            setValue(v, updateOn = kvarUpdateEvent)
            _valueKvar = v
        }

    /**
     * Automatically update `toBind` with the value of this INPUT element when `updateOn` event occurs.
     */
    fun setValue(toBind : KVar<String>, updateOn : String = "input") {
        setValue(toBind as KVal<String>)

        // TODO: Would be really nice if it just did a diff on the value and sent that, rather than the
        //       entire value each time PARTICULARLY for large inputs
        on(retrieveJs = "${jsExpression}.value").event(updateOn, ONReceiver.Event::class) {
            toBind.value = it.retrieved ?: throw RuntimeException("No value was retrieved")
        }
    }
}

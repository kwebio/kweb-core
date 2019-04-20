package io.kweb.dom.element.creation.tags

import com.github.salomonbrys.kotson.toJson
import io.kweb.dom.attributes.*
import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.events.ONReceiver
import io.kweb.dom.element.events.on
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
open class InputElement(val element: Element) : Element(element) {
    fun checked(checked: Boolean) = setAttribute("checked", checked)
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
        setValue(v, updateOn = "input")
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

    fun select() = element.execute("$jsExpression.select();")

    fun setReadOnly(ro: Boolean) = element.execute("$jsExpression.readOnly = $ro;")
}

enum class InputType {
    button, checkbox, color, date, datetime, email, file, hidden, image, month, number, password, radio, range, reset, search, submit, tel, text, time, url, week
}

fun ElementCreator<Element>.label(forId: String?, attributes: Map<String, Any> = attr) = LabelElement(element("label", attributes = attributes.set("for", forId)))

fun ElementCreator<Element>.label(for_: Element? = null, attributes: Map<String, Any> = attr) = LabelElement(element("label", attributes = attributes.set("for", for_?.id)))

open class LabelElement(wrapped: Element) : Element(wrapped)

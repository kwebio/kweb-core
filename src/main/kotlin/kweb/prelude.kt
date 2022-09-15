package kweb

import io.ktor.server.routing.*
import io.mola.galimatias.URL
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kweb.html.ElementReader
import kweb.html.HeadElement
import kweb.html.TitleElement
import kweb.html.events.Event
import kweb.html.fileUpload.FileFormInput
import kweb.routing.PathTemplate
import kweb.routing.RouteReceiver
import kweb.routing.UrlToPathSegmentsRF
import kweb.state.*
import kweb.util.json
import kweb.util.pathQueryFragment
import kotlin.collections.set

/*
 * Mostly extension functions (and any simple classes they depend on), placed here such that an `import kweb.*`
 * will pick them up.
 */

fun ElementCreator<HeadElement>.title(
        attributes: Map<String, JsonPrimitive> = emptyMap(),
        new: (ElementCreator<TitleElement>.() -> Unit)? = null
): TitleElement {
    return TitleElement(element("title", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class ULElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.ul(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<ULElement>.() -> Unit)? = null
): ULElement {
    return ULElement(element("ul", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class OLElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.ol(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<OLElement>.() -> Unit)? = null
): OLElement {
    return OLElement(element("ol", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class LIElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.li(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<LIElement>.() -> Unit)? = null
): LIElement {
    return LIElement(element("li", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class ButtonElement(val wrapped: Element) : Element(wrapped)
enum class ButtonType {
    button, reset, submit
}

fun ElementCreator<Element>.button(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    type: ButtonType? = ButtonType.button,
    autofocus: Boolean? = null,
    new: (ElementCreator<ButtonElement>.() -> Unit)? = null,
): ButtonElement {
    return ButtonElement(
        element(
            "button", attributes
                .set("type", JsonPrimitive(type?.name))
                .set("autofocus", JsonPrimitive(autofocus))
        )
    ).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class SpanElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.span(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<SpanElement>.() -> Unit)? = null
): SpanElement {
    return SpanElement(element("span", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class DivElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.div(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<DivElement>.() -> Unit)? = null
): DivElement {
    return DivElement(element("div", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class IElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.i(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<IElement>.() -> Unit)? = null
): IElement {
    return IElement(element("i", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class FormElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.form(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<FormElement>.() -> Unit)? = null
): FormElement {
    return FormElement(element("form", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class AElement(parent: Element) : Element(parent) {

    /**
     * A convenience property to set the href attribute of this anchor element. If the value begins with
     * "/" (a relative URL) then this will override the default click behavior and set the [WebBrowser.url]
     * to the appropriate value, avoiding a page refresh.
     *
     * *Note:* This property may only be set, attempting to read this property will throw an error.
     *
     * TODO: Should this be a KVar rather than a String?
     */
    var href : String? get() {
        error("The href property may only be set, but not read")
    }
    set(hrefValue) {
        if (hrefValue != null) {
            setAttribute("href", hrefValue)
            if (hrefValue.startsWith('/')) {
                this.on(preventDefault = true).click {
                    this.browser.url.value = hrefValue
                }
            }
        }
    }
}

fun ElementCreator<Element>.a(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    href: String? = null,
    new: (ElementCreator<AElement>.() -> Unit)? = null
): AElement {
    return AElement(element("a", attributes.set("href", JsonPrimitive(href)))).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}


open class OptionElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.option(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<OptionElement>.() -> Unit)? = null
): OptionElement {
    return OptionElement(element("option", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class H1Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h1(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<H1Element>.() -> Unit)? = null
): H1Element {
    return H1Element(element("h1", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class H2Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h2(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<H2Element>.() -> Unit)? = null
): H2Element {
    return H2Element(element("h2", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class H3Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h3(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<H3Element>.() -> Unit)? = null
): H3Element {
    return H3Element(element("h3", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class H4Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h4(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<H4Element>.() -> Unit)? = null
): H4Element {
    return H4Element(element("h4", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class H5Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h5(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<H5Element>.() -> Unit)? = null
): H5Element {
    return H5Element(element("h5", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class PElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.p(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<PElement>.() -> Unit)? = null
): PElement {
    return PElement(element("p", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class NavElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.nav(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<NavElement>.() -> Unit)? = null
): NavElement {
    return NavElement(element("nav", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class SectionElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.section(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<SectionElement>.() -> Unit)? = null
): SectionElement {
    return SectionElement(element("section", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class ImageElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.img(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<ImageElement>.() -> Unit)? = null
): ImageElement {
    return ImageElement(element("img", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class CanvasElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.canvas(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    width: Int, height: Int,
    new: (ElementCreator<CanvasElement>.() -> Unit)? = null
): CanvasElement {
    return CanvasElement(
        element(
            "canvas",
            attributes
                .set("width", JsonPrimitive(width)).set("height", JsonPrimitive(height))
        )
    ).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class BrElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.br(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<BrElement>.() -> Unit)? = null
): BrElement {
    return BrElement(element("br", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class MetaElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.meta(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    name: String, content: String, httpEquiv: String? = null, charset: String? = null,
    new: (ElementCreator<MetaElement>.() -> Unit)? = null
): MetaElement {
    return MetaElement(
        element(
            "meta", attributes.set("name", name.json)
                .set("content", content.json)
                .set("http-equiv", JsonPrimitive(httpEquiv))
                .set("charset", JsonPrimitive(charset))
        )
    ).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

open class InputElement(override val element: Element, initialValue: String? = null) :
    ValueElement(element, initialValue = initialValue) {
    fun select() = element.callJsFunction("document.getElementById({}).select();", id.json)

    fun setSelectionRange(start: Int, end: Int) = element.callJsFunction(
            "document.getElementById({}).setSelectionRange({}, {});",
            id.json, start.json, end.json)

    fun setReadOnly(ro: Boolean) = element.callJsFunction("document.getElementById({}).readOnly = {};",
            id.json, ro.json)

    fun checked(initialValue : Boolean = false) : KVar<Boolean> {
        val kv = bind(accessor = { "document.getElementById(\"$it\").checked" }, updateOnEvent = "change",
            initialValue = JsonPrimitive(initialValue))
        return kv.map(object : ReversibleFunction<JsonElement, Boolean>("") {
            override fun invoke(from: JsonElement) = from.jsonPrimitive.boolean

            override fun reverse(original: JsonElement, change: Boolean) = JsonPrimitive(change)
        })
    }
}

enum class InputType {
    button, checkbox, color, date, datetime, email, file, hidden, image, month, number, password, radio, range, reset, search, submit, tel, text, time, url, week
}

fun ElementCreator<Element>.input(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    type: InputType? = null,
    name: String? = null,
    initialValue: String? = null,
    size: Int? = null,
    placeholder: String? = null,
    new: (ElementCreator<InputElement>.() -> Unit)? = null
): InputElement {
    return InputElement(
        element(
            "input", attributes.set("type", JsonPrimitive(type?.name))
                .set("name", JsonPrimitive(name))
                .set("value", JsonPrimitive(initialValue))
                .set("placeholder", JsonPrimitive(placeholder))
                .set("size", JsonPrimitive(size))
        ), initialValue = initialValue
    ).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

fun ElementCreator<Element>.textArea(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    rows: Int? = null, cols: Int? = null, required: Boolean? = null,
    new: (ElementCreator<TextAreaElement>.() -> Unit)? = null
): TextAreaElement {
    return TextAreaElement(
        element(
            "textArea", attributes.set("rows", JsonPrimitive(rows))
                .set("cols", JsonPrimitive(cols))
                .set("required", JsonPrimitive(required))
        )
    ).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

/**
 * [<SELECT>](https://www.w3schools.com/tags/tag_select.asp)
 */
class SelectElement(parent: Element, initialValue: String? = null) :
    ValueElement(parent, kvarUpdateEvent = "change", initialValue = initialValue)

/**
 * [<SELECT>](https://www.w3schools.com/tags/tag_select.asp)
 *
 * // @sample select_sample
 */
fun ElementCreator<Element>.select(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    name: String? = null, required: Boolean? = null,
    new: (ElementCreator<SelectElement>.() -> Unit)? = null
): SelectElement {
    return SelectElement(
        element(
            "select", attributes
                .set("name", JsonPrimitive(name))
                .set("required", JsonPrimitive(required))
        )
    ).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

private fun select_sample() {
    val server: Kweb = Kweb(port= 7668) {
        doc.body {
            val select = select(name = "pets") {
                option().setAttribute("value", "dog").text("Dog")
                option().setAttribute("value", "cat").text("Cat")
            }
            select.value.addListener { old, new ->
                println("Value of select changed from $old to $new")
            }
        }
    }
}

/**
 * https://www.w3schools.com/tags/tag_textarea.asp
 */
open class TextAreaElement(parent: Element, initialValue: String? = null) : ValueElement(parent, initialValue = initialValue) {
    //TODO ValueElement already provides a way to get the value of an element. I'm not sure why this function is here.
    //But, something needs to be done with it.
    override val read get() = TextAreaElementReader(this)
}

/**
 * https://www.w3schools.com/tags/tag_textarea.asp
 */
fun ElementCreator<Element>.textArea(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<TextAreaElement>.() -> Unit)? = null
): TextAreaElement {
    return TextAreaElement(element("textArea", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

//TODO I'm not quite sure how much information I should be putting in this message, or how an end user should replace this
//It seems like we should rewrite TextAreaElement.get() so they can use that, or tell them to just use Kvars.
@Deprecated("ElementReader has been deprecated. Use TextAreaElement.get() instead")
open class TextAreaElementReader(val element: TextAreaElement) : ElementReader(element) {
    suspend fun getValue() : String {
        //A TextArea should only ever contain a String. So using toString() here should be safe.
        // We could add some error handling here if we wanted to though.
        return receiver.callJsFunctionWithResult("return document.getElementById({}).innerText;", JsonPrimitive(element.id)).toString()
    }
    //val value get() = receiver.callJsFunctionWithResult("return document.getElementById({}).innerText;", element.id)
}

open class LabelElement(wrapped: Element) : Element(wrapped)

fun ElementCreator<Element>.label(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<LabelElement>.() -> Unit)? = null
): LabelElement {
    return LabelElement(element("label", attributes)).also {
        if (new != null) new(ElementCreator(parent = it, insertBefore = null))
    }
}

/**
 * Abstract class for the various elements that have a `value` attribute and which support `change` and `input` events.
 *
 * @param kvarUpdateEvent The [value] of this element will update on this event, defaults to [input](https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/input_event)
 */
abstract class ValueElement(open val element: Element, val kvarUpdateEvent: String = "input",
                            val initialValue: String? = null) : Element(element) {
    val valueJsExpression : String by lazy { "document.getElementById(\"$id\").value" }

    suspend fun getValue():String = element.
    callJsFunctionWithResult("return document.getElementById({}).value;", outputMapper = { when (it) {
        is JsonPrimitive -> it.content
        else -> error("Needs to be JsonPrimitive")
    } }, id.json)
        ?: error("Not sure why .evaluate() would return null")

    //language=JavaScript
    fun setValue(newValue: String) = element.callJsFunction("document.getElementById({}).value = {};",
            element.id.json, newValue.json)
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

    @Volatile
    private var _valueKvar: KVar<String>? = null

    /**
     * A KVar bidirectionally synchronized with the [value of a select element](https://www.w3schools.com/jsref/prop_select_value.asp).
     * This [KVar] will update if the select element is changed (depending on [kvarUpdateEvent]), and will modify the element value
     * if the KVar is changed.
     *
     * // @sample select_sample
     */
    var value: KVar<String>
        get() {
            synchronized(this) {
                if (_valueKvar == null) {
                    value = KVar(initialValue ?: "")
                }
            }
            return _valueKvar!!
        }
        set(v) {
            if (_valueKvar != null) error("`value` may only be set once, and cannot be set after it has been retrieved")
            updateKVar(v, updateOn = kvarUpdateEvent)
            _valueKvar = v
        }


    /**
     * Automatically update `toBind` with the value of this INPUT element when `updateOn` event occurs.
     */

    @Serializable
    data class DiffData(val prefixEndIndex: Int, val postfixOffset: Int, val diffString: String)

    private fun applyDiff(oldString: String, diffData: DiffData) : String {

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

    private fun updateKVar(toBind: KVar<String>, updateOn: String = "input") {
        on(
            //language=JavaScript
            retrieveJs = "get_diff_changes(document.getElementById(\"${element.id}\"))")
            .event<Event>(updateOn) {
            //TODO, this check shouldn't be necessary. It should be impossible for get_diff_changes() to return a null,
            //but we had a null check previously, so I went ahead and added it.
            if (it.retrieved != JsonNull) {
                val diffDataJson = it.retrieved
                val diffData = Json.decodeFromJsonElement(DiffData.serializer(), diffDataJson)
                toBind.value = applyDiff(toBind.value, diffData)
            }
        }
    }

}

/******************************
 * Route extension
 ******************************/

fun ElementCreator<*>.route(routeReceiver: RouteReceiver.() -> Unit) {
    val rr = RouteReceiver()
    routeReceiver(rr)
    val pathKVar: KVar<List<String>> = this.browser.url.map(UrlToPathSegmentsRF)
    val matchingTemplate: KVal<PathTemplate?> = pathKVar.map { path ->
        val size = if (path != listOf("")) path.size else 0
        val templatesOfSameLength = rr.templatesByLength[size]
        val tpl = templatesOfSameLength?.keys?.firstOrNull { tpl ->
            tpl.isEmpty() || tpl.withIndex().all {
                val tf = it.value.kind != RoutingPathSegmentKind.Constant || path[it.index] == it.value.value
                tf
            }
        }
        tpl
    }

    render(matchingTemplate) { template ->
        if (template != null) {
            val parameters = HashMap<String, KVar<String>>()
            for ((pos, part) in template.withIndex()) {
                if (part.kind == RoutingPathSegmentKind.Parameter) {
                    val str = part.value
                    val paramKVar = pathKVar[pos]
                    closeOnElementCreatorCleanup(paramKVar)
                    parameters[str.substring(str.indexOf('{') + 1, str.indexOf('}'))] = paramKVar
                }
            }

            val pathRenderer = rr.templatesByLength[template.size]?.get(template)

            if (pathRenderer != null) {
                pathRenderer(this, parameters)
            } else {
                error("Unable to find pathRenderer for template $template")
            }
        } else {
            rr.notFoundReceiver.invoke(this, this.browser.gurl.path.value)
        }
    }
}

/******************************
 * KVar extensions
 ******************************/

operator fun <T : Any> KVar<List<T>>.get(pos: Int): KVar<T> {
    return this.map(object : ReversibleFunction<List<T>, T>("get($pos)") {
        override fun invoke(from: List<T>): T {
            return try {
                from[pos]
            } catch (e: IndexOutOfBoundsException) {
                throw IndexOutOfBoundsException("Index $pos out of bounds in list $from")
            }
        }

        override fun reverse(original: List<T>, change: T) = original
            .subList(0, pos)
            .plus(change)
            .plus(original.subList(pos + 1, original.size))
    })
}

operator fun <K : Any, V : Any> KVar<Map<K, V>>.get(k: K): KVar<V?> {
    return this.map(object : ReversibleFunction<Map<K, V>, V?>("map[$k]") {
        override fun invoke(from: Map<K, V>): V? = from[k]

        override fun reverse(original: Map<K, V>, change: V?): Map<K, V> {
            return if (change != null) {
                original + (k to change)
            } else {
                original - k
            }
        }
    })
}

fun <T : Any> KVar<List<T>>.subList(fromIx: Int, toIx: Int): KVar<List<T>> = this.map(object : ReversibleFunction<List<T>, List<T>>("subList($fromIx, $toIx)") {
    override fun invoke(from: List<T>): List<T> = from.subList(fromIx, toIx)

    override fun reverse(original: List<T>, change: List<T>): List<T> {
        return original.subList(0, fromIx) + change + original.subList(toIx, original.size)
    }
})

fun <T : Any> KVal<List<T>>.subList(fromIx: Int, toIx: Int): KVal<List<T>> = this.map { it.subList(fromIx, toIx) }

enum class Scheme {
    http, https
}

private val prx = "/".toRegex()

val KVar<URL>.path
    get() = this.map(object : ReversibleFunction<URL, String>("URL.path") {

        override fun invoke(from: URL): String = from.path()

        override fun reverse(original: URL, change: String): URL =
            original.withPath(change)

    })

val KVar<URL>.query
    get() = this.map(object : ReversibleFunction<URL, String?>("URL.query") {

        override fun invoke(from: URL): String? = from.query()

        override fun reverse(original: URL, change: String?): URL =
            original.withQuery(change)

    })

val KVar<URL>.pathSegments
    get() = this.map(object : ReversibleFunction<URL, List<String>>("URL.pathSegments") {

        override fun invoke(from: URL): List<String> {
            return from.pathSegments()
        }

        override fun reverse(original: URL, change: List<String>): URL {
            return original.withPath("/" + if (change.isEmpty()) "" else change.joinToString(separator = "/"))
        }

    })

/**
 * Given the URI specification:
 *
 * `URI = scheme:[//authority]path[?query][#fragment]`
 *
 * The `pqf` refers to the `path[?query][#fragment]` and can be used to change the path, query, and/or fragment
 * of the URL in one shot.
 */
val KVar<URL>.pathQueryFragment
    get() = this.map(object : ReversibleFunction<URL, String>("URL.pathQueryFragment") {
        override fun invoke(from: URL): String {
            return from.pathQueryFragment
        }

        override fun reverse(original: URL, change: String): URL {
            return original.resolve(change)
        }
    })

infix operator fun KVar<String>.plus(s: String) = this.map { it + s }
infix operator fun String.plus(sKV: KVar<String>) = sKV.map { this + it }

fun KVar<String>.toInt() = this.map(object : ReversibleFunction<String, Int>(label = "KVar<String>.toInt()") {
    override fun invoke(from: String) = from.toInt()

    override fun reverse(original: String, change: Int): String {
        return change.toString()
    }
})

/**
 * Render each element of a List
 */
fun <T : Any> ElementCreator<*>.renderEach(list: KVar<List<T>>, block: ElementCreator<Element>.(value: KVar<T>) -> Unit) {
    /*
     * TODO: This will currently re-render the collection if the list size changes, rather than modifying existing
     *       DOM elements - this is inefficient.
     */
    render(list.map { it.size }) { size ->
        for (ix in 0 until size) {
            block(list[ix])
        }
    }
}

/**
 * Create a [FileReader](https://developer.mozilla.org/en-US/docs/Web/API/FileReader)
 *
 * // @sample fileReaderSample
 */
fun ElementCreator<*>.fileInput(name: String? = null, initialValue: String? = null, size: Int? = null, placeholder: String? = null, attributes: Map<String, JsonPrimitive> = attr): FileFormInput {
    val inputElement = input(attributes, InputType.file, name, initialValue, size, placeholder)
    val formInput = FileFormInput()
    formInput.setInputElement(inputElement)
    return formInput
}

fun fileReaderSample() {
    val imageString = KVar(JsonPrimitive(""))
    Kweb(port = 123) {
        doc.body.new {
            val input = fileInput()
            input.onFileSelect {
                input.retrieveFile {
                    imageString.value = JsonPrimitive(it.base64Content)
                }
            }
            img().setAttribute("src", imageString)
        }
    }
}
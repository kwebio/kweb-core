package kweb

import io.ktor.server.routing.*
import io.mola.galimatias.URL
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
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
    new: (ElementCreator<TitleElement>.(TitleElement) -> Unit)? = null
): TitleElement {
    return TitleElement(element("title", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class ULElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.ul(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<ULElement>.(ULElement) -> Unit)? = null
): ULElement {
    return ULElement(element("ul", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class OLElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.ol(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<OLElement>.(OLElement) -> Unit)? = null
): OLElement {
    return OLElement(element("ol", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class LIElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.li(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<LIElement>.(LIElement) -> Unit)? = null
): LIElement {
    return LIElement(element("li", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
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
    new: (ElementCreator<ButtonElement>.(ButtonElement) -> Unit)? = null,
): ButtonElement {
    return ButtonElement(
        element(
            "button", attributes
                .set("type", JsonPrimitive(type?.name))
                .set("autofocus", JsonPrimitive(autofocus))
        )
    ).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class SpanElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.span(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<SpanElement>.(SpanElement) -> Unit)? = null
): SpanElement {
    return SpanElement(element("span", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class DivElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.div(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<DivElement>.(DivElement) -> Unit)? = null
): DivElement {
    return DivElement(element("div", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class IElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.i(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<IElement>.(IElement) -> Unit)? = null
): IElement {
    return IElement(element("i", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class FormElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.form(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<FormElement>.(FormElement) -> Unit)? = null
): FormElement {
    return FormElement(element("form", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class AElement(parent: Element, val preventDefault: Boolean = true) : Element(parent) {
    /**
     * A convenience property to set the href attribute of this anchor element. If [preventDefault] is enabled
     * and the value begins with "/" (a relative URL) then this will override the default click behavior and
     * set the [WebBrowser.url] to the appropriate value, avoiding a page refresh.
     *
     * *Note:* This property may only be set, attempting to read this property will throw an error.
     *
     * TODO: Should this be a KVar rather than a String?
     */
    var href: String?
        get() {
            error("The href property may only be set, but not read")
        }
        set(hrefValue) {
            if (hrefValue != null) {
                set("href", hrefValue)
                if (preventDefault && hrefValue.startsWith('/')) {
                    this.on(preventDefault = true).click {
                        this.browser.url.value = hrefValue
                    }
                }
            }
        }
}

/**
 * Create an anchor element.
 *
 * By default, following a href does not lead to a full page load, just to a `window.url` change,
 * that will trigger a page update. You can override this behavior for each AElement by setting the
 * [preventDefault] parameter to false, which will lead to the "standard" href behavior of full page loads.
 *
 * @param href @see [AElement.href]
 */
fun ElementCreator<Element>.a(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    href: String? = null,
    preventDefault : Boolean = true,
    new: (ElementCreator<AElement>.(AElement) -> Unit)? = null
): AElement {
    return AElement(element("a"), preventDefault = preventDefault).also {
        if (href != null) it.href = href
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
        attributes.forEach { (k, v) -> it[k] = v }
    }
}


open class OptionElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.option(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<OptionElement>.(OptionElement) -> Unit)? = null
): OptionElement {
    return OptionElement(element("option", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class H1Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h1(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<H1Element>.(H1Element) -> Unit)? = null
): H1Element {
    return H1Element(element("h1", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class H2Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h2(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<H2Element>.(H2Element) -> Unit)? = null
): H2Element {
    return H2Element(element("h2", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class H3Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h3(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<H3Element>.(H3Element) -> Unit)? = null
): H3Element {
    return H3Element(element("h3", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class H4Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h4(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<H4Element>.(H4Element) -> Unit)? = null
): H4Element {
    return H4Element(element("h4", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class H5Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h5(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<H5Element>.(H5Element) -> Unit)? = null
): H5Element {
    return H5Element(element("h5", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class PElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.p(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<PElement>.(PElement) -> Unit)? = null
): PElement {
    return PElement(element("p", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class NavElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.nav(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<NavElement>.(NavElement) -> Unit)? = null
): NavElement {
    return NavElement(element("nav", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class SectionElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.section(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<SectionElement>.(SectionElement) -> Unit)? = null
): SectionElement {
    return SectionElement(element("section", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class ImageElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.img(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<ImageElement>.(ImageElement) -> Unit)? = null
): ImageElement {
    return ImageElement(element("img", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class CanvasElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.canvas(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    width: Int, height: Int,
    new: (ElementCreator<CanvasElement>.(CanvasElement) -> Unit)? = null
): CanvasElement {
    return CanvasElement(
        element(
            "canvas",
            attributes
                .set("width", JsonPrimitive(width)).set("height", JsonPrimitive(height))
        )
    ).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class BrElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.br(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<BrElement>.(BrElement) -> Unit)? = null
): BrElement {
    return BrElement(element("br", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class MetaElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.meta(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    name: String? = null, content: String? = null, httpEquiv: String? = null, charset: String? = null,
    new: (ElementCreator<MetaElement>.(MetaElement) -> Unit)? = null
): MetaElement {
    return MetaElement(
        element(
            "meta", attributes.set("name", JsonPrimitive(name))
                .set("content", JsonPrimitive(content))
                .set("http-equiv", JsonPrimitive(httpEquiv))
                .set("charset", JsonPrimitive(charset))
        )
    ).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

/********************************
 * Viewport and related classes *
 ********************************/

fun ElementCreator<HeadElement>.viewport(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    width : ViewportWidth = ViewportWidth.DeviceWidth,
    height : ViewportHeight = ViewportHeight.DeviceHeight,
    initialScale : Double = 1.0,
    minimumScale : Double = 0.1,
    maximumScale : Double = 10.0,
    userScalable : UserScalable = UserScalable.Yes,
    new: (ElementCreator<MetaElement>.(MetaElement) -> Unit)? = null
): MetaElement {
    return MetaElement(
        element(
            "meta", attributes.set("name", "viewport".json)
                .set("content", JsonPrimitive(
                    "width=${width.asString}, height=${height.value}, initial-scale=$initialScale, minimum-scale=$minimumScale, maximum-scale=$maximumScale, user-scalable=${userScalable.value}"
                ))
        )
    ).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

sealed class UserScalable(val value: String) {
    object Yes : UserScalable("yes")
    object No : UserScalable("no")
}

sealed class ViewportWidth {
    abstract val asString : String

    object DeviceWidth : ViewportWidth() {
        override val asString: String
            get() = "device-width"
    }
    data class Width(val width: Int) : ViewportWidth() {
        override val asString: String
            get() = "$width"
    }
}

sealed class ViewportHeight {
    abstract val value : String

    object DeviceHeight : ViewportHeight() {
        override val value: String
            get() = "device-height"
    }
    data class Height(val height: Int) : ViewportHeight() {
        override val value: String
            get() = "$height"
    }
}

open class InputElement(override val element: Element, initialValue: String? = null) :
    ValueElement(element, initialValue = initialValue) {
    fun select() {
        element.browser.callJsFunction("document.getElementById({}).select();", id.json)
    }

    fun setSelectionRange(start: Int, end: Int) {
        element.browser.callJsFunction(
            "document.getElementById({}).setSelectionRange({}, {});",
            id.json, start.json, end.json
        )
    }

    fun setReadOnly(ro: Boolean) {
        element.browser.callJsFunction(
            "document.getElementById({}).readOnly = {};",
            id.json, ro.json
        )
    }

    fun checked(initialValue: Boolean = false): KVar<Boolean> {
        val kv = bind(
            accessor = { "document.getElementById(\"$it\").checked" }, updateOnEvent = "change",
            initialValue = JsonPrimitive(initialValue)
        )
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
    required: Boolean? = null,
    new: (ElementCreator<InputElement>.(InputElement) -> Unit)? = null
): InputElement {
    return InputElement(
        element(
            "input", attributes.set("type", JsonPrimitive(type?.name))
                .set("name", JsonPrimitive(name))
                .set("value", JsonPrimitive(initialValue))
                .set("placeholder", JsonPrimitive(placeholder))
                .set("size", JsonPrimitive(size))
                .set("required", JsonPrimitive(required))
        ), initialValue = initialValue
    ).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

fun ElementCreator<Element>.link(
    rel: LinkRelationship,
    href: java.net.URL,
    hreflang: String? = null,
    attributes: Map<String, JsonPrimitive> = emptyMap(),
): Element {
    return LinkElement(
        element(
            "link",
            attributes = attributes
                .set("rel", rel.name.json)
                .set("href", href.toString().json)
                .set("hreflang", JsonPrimitive(hreflang))
        )
    )
}

enum class LinkRelationship {
    alternate, author, bookmark, help, icon, license, next, nofollow, noreferrer, prefetch, prev, search, stylesheet, tag, `dns-prefetch`, preconnect, preload
}

class LinkElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.textArea(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    rows: Int? = null, cols: Int? = null, required: Boolean? = null,
    initialValue: String? = null,
    new: (ElementCreator<TextAreaElement>.(TextAreaElement) -> Unit)? = null
): TextAreaElement {
    return TextAreaElement(
        element(
            "textArea", attributes.set("rows", JsonPrimitive(rows))
                .set("cols", JsonPrimitive(cols))
                .set("required", JsonPrimitive(required))
        ), initialValue = initialValue
    ).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

/**
 * [<SELECT>](https://www.w3schools.com/tags/tag_select.asp)
 */
class SelectElement(parent: Element, initialValue: String? = null) :
    ValueElement(parent, kvarUpdateEvent = "change", initialValue = initialValue)

/**
 * [<SELECT>](https://www.w3schools.com/tags/tag_select.asp)
 */
fun ElementCreator<Element>.select(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    name: String? = null, required: Boolean? = null,
    initialValue: String? = null,
    new: (ElementCreator<SelectElement>.(SelectElement) -> Unit)? = null
): SelectElement {
    return SelectElement(
        element(
            "select", attributes
                .set("name", JsonPrimitive(name))
                .set("required", JsonPrimitive(required))
        ), initialValue = initialValue
    ).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

/**
 * https://www.w3schools.com/tags/tag_textarea.asp
 */
class TextAreaElement(parent: Element, initialValue: String? = null) : ValueElement(parent, initialValue = initialValue)

/**
 * https://www.w3schools.com/tags/tag_textarea.asp
 */
fun ElementCreator<Element>.textArea(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<TextAreaElement>.(TextAreaElement) -> Unit)? = null
): TextAreaElement {
    return TextAreaElement(element("textArea", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class LabelElement(wrapped: Element) : Element(wrapped) {
    fun setFor(forId: String) = this.set("for", forId)
    fun setFor(forId: KVal<String>) = set("for", forId.map { JsonPrimitive(it) })

}

fun ElementCreator<Element>.label(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<LabelElement>.(LabelElement) -> Unit)? = null
): LabelElement {
    return LabelElement(element("label", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

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
            "document.getElementById({}).value = {};",
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

    @Volatile
    private var _valueKvar: KVar<String>? = null

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
            synchronized(this) {
                if (_valueKvar == null) {
                    value = KVar(initialValue ?: "")
                    this.creator?.onCleanup(true) {
                        value.close(CloseReason("Parent element closed"))
                    }
                    attachListeners(value)
                }
            }
            return _valueKvar!!
        }
        set(v) {
            if (_valueKvar != null) error("`value` may only be set once, and cannot be set after it has been retrieved")
            updateKVar(v, updateOn = kvarUpdateEvent)
            attachListeners(v)
            setValue(v.value)
            _valueKvar = v
        }

    private fun attachListeners(kv : KVar<String>) {
        val handle = kv.addListener { _, newValue ->
            setValue(newValue)
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

    private fun updateKVar(toBind: KVar<String>, updateOn: String = "input") {
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

fun <T : Any> KVar<List<T>>.subList(fromIx: Int, toIx: Int): KVar<List<T>> =
    this.map(object : ReversibleFunction<List<T>, List<T>>("subList($fromIx, $toIx)") {
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

infix fun KVar<String>.attr(s: String) = this.map { it + s }
infix fun String.attr(sKV: KVar<String>) = sKV.map { this + it }

fun KVar<String>.toInt() = this.map(object : ReversibleFunction<String, Int>(label = "KVar<String>.toInt()") {
    override fun invoke(from: String) = from.toInt()

    override fun reverse(original: String, change: Int): String {
        return change.toString()
    }
})

/**
 * Render each element of a List
 */
@Deprecated("Use kweb.state.renderEach instead", ReplaceWith("renderEach(list, block)", "kweb.state.renderEach"))
fun <T : Any> ElementCreator<*>.renderEach(
    list: KVar<List<T>>,
    block: ElementCreator<Element>.(value: KVar<T>) -> Unit
) {
    /*
     * TODO: This will currently re-render the collection if the list size changes, rather than modifying existing
     *       DOM elements - this is inefficient and should use renderEach() with an ObservableList instead.
     */
    render(list.map { it.size }) { size ->
        for (ix in 0 until size) {
            block(list[ix])
        }
    }
}

/**
 * Create a [FileReader](https://developer.mozilla.org/en-US/docs/Web/API/FileReader)
 */
fun ElementCreator<*>.fileInput(
    name: String? = null,
    initialValue: String? = null,
    size: Int? = null,
    placeholder: String? = null,
    attributes: Map<String, JsonPrimitive> = attr
): FileFormInput {
    val inputElement = input(attributes, InputType.file, name, initialValue, size, placeholder)
    val formInput = FileFormInput()
    formInput.setInputElement(inputElement)
    return formInput
}

fun ElementCreator<Element>.table(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<TableElement>.(TableElement) -> Unit)? = null
): TableElement {
    return TableElement(element("table", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class TableElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.thead(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<TheadElement>.(TheadElement) -> Unit)? = null
): TheadElement {
    return TheadElement(element("thead", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class TheadElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.th(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<ThElement>.(ThElement) -> Unit)? = null
): ThElement {
    return ThElement(element("th", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class ThElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.tbody(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<TbodyElement>.(TbodyElement) -> Unit)? = null
): TbodyElement {
    return TbodyElement(element("tbody", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class TbodyElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.tr(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<TrElement>.(TrElement) -> Unit)? = null
): TrElement {
    return TrElement(element("tr", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

open class TrElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.td(
    attributes: Map<String, JsonPrimitive> = emptyMap(),
    new: (ElementCreator<TdElement>.(TdElement) -> Unit)? = null
): TdElement {
    return TdElement(element("td", attributes)).also {
        if (new != null) new(ElementCreator(element = it, insertBefore = null), it)
    }
}

class TdElement(parent: Element) : Element(parent)

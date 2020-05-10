package kweb

import com.github.salomonbrys.kotson.toJson
import io.ktor.routing.RoutingPathSegmentKind
import io.mola.galimatias.URL
import kweb.html.ElementReader
import kweb.html.HeadElement
import kweb.html.TitleElement
import kweb.html.events.ONReceiver
import kweb.routing.PathTemplate
import kweb.routing.RouteReceiver
import kweb.routing.UrlToPathSegmentsRF
import kweb.state.*
import java.util.concurrent.CompletableFuture

/*
 * Mostly extension functions (and any simple classes they depend on), placed here such that an `import kweb.*`
 * will pick them up.
 */

fun ElementCreator<HeadElement>.title(attributes: Map<String, Any> = attr) = TitleElement(element("title", attributes))

open class ULElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.ul(attributes: Map<String, Any> = attr) = ULElement(element("ul", attributes))

open class LIElement(parent: Element) : Element(parent)

fun ElementCreator<ULElement>.li(attributes: Map<String, Any> = attr) = LIElement(element("li", attributes))

open class ButtonElement(val wrapped: Element) : Element(wrapped)
enum class ButtonType {
    button, reset, submit
}

fun ElementCreator<Element>.button(attributes: Map<String, Any> = attr, type: ButtonType? = ButtonType.button, autofocus: Boolean? = null): ButtonElement {
    return ButtonElement(element("button", attributes
            .set("type", type?.name)
            .set("autofocus", autofocus)
    ))
}


open class SpanElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.span(attributes: Map<String, Any> = attr) = SpanElement(element("span", attributes))

open class DivElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.div(attributes: Map<String, Any> = attr) = DivElement(element("div", attributes))

open class IElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.i(attributes: Map<String, Any> = attr) = IElement(element("i", attributes))

open class FormElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.form(attributes: Map<String, Any> = attr) = FormElement(element("form", attributes))

open class AElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.a(attributes: Map<String, Any> = attr, href: String? = "#") = AElement(element("a",
        attributes.set("href", href)))


fun ElementCreator<Element>.textArea(rows: Int? = null, attributes: Map<String, Any> = attr) = TextAreaElement(element("textarea", attributes.set("rows", rows?.toString()).set("type", "text")))

open class OptionElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.option(attributes: Map<String, Any> = attr) = OptionElement(element("option", attributes))


open class H1Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h1(attributes: Map<String, Any> = attr) = H1Element(element("h1", attributes))

open class H2Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h2(attributes: Map<String, Any> = attr) = H2Element(element("h2", attributes))

open class H3Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h3(attributes: Map<String, Any> = attr) = H3Element(element("h3", attributes))

open class H4Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h4(attributes: Map<String, Any> = attr) = H4Element(element("h4", attributes))

open class H5Element(parent: Element) : Element(parent)

fun ElementCreator<Element>.h5(attributes: Map<String, Any> = attr) = H5Element(element("h5", attributes))

open class PElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.p(attributes: Map<String, Any> = attr) = PElement(element("p", attributes))

open class NavElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.nav(attributes: Map<String, Any> = attr) = NavElement(element("nav", attributes))

open class SectionElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.section(attributes: Map<String, Any> = attr) = SectionElement(element("section", attributes))

open class ImageElement(parent: Element) : Element(parent)

/**
 * @param src The image source. The source must be from an external server since Kweb doesn't handle internal routing yet
 * @param attributes Extra attributes you want to add
 */
fun ElementCreator<Element>.img(src: String? = null, attributes: Map<String, Any> = attr) =
        ImageElement(element("img", attributes.set("src", src)))


open class CanvasElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.canvas(width: Int, height: Int) = CanvasElement(element("canvas", mapOf("width" to width, "height" to height)))

open class BrElement(parent: Element) : Element(parent)

fun ElementCreator<Element>.br(attributes: Map<String, Any> = attr) = BrElement(element("br", attributes))

open class MetaElement(parent: Element) : Element(parent)

fun ElementCreator<HeadElement>.meta(name: String, content: String, httpEquiv: String? = null, charset: String? = null, attributes: Map<String, Any> = attr): MetaElement {
    return MetaElement(element("meta", attributes
            .set("name", name)
            .set("content", content)
            .set("http-equiv", httpEquiv)
            .set("charset", charset)
    ))
}

fun ElementCreator<Element>.input(type: InputType? = null, name: String? = null, initialValue: String? = null, size: Int? = null, placeholder: String? = null, attributes: Map<String, Any> = attr): InputElement {
    return InputElement(element("input", attributes = attributes
            .set("type", type?.name)
            .set("name", name)
            .set("value", initialValue)
            .set("placeholder", placeholder)
            .set("size", size)
    ))
}

open class InputElement(override val element: Element) : ValueElement(element) {
    fun checked(checked: Boolean) = setAttributeRaw("checked", checked)


    fun select() = element.execute("$jsExpression.select();")

    /*

     copyText.setSelectionRange(0, 99999);
     */

    fun setSelectionRange(start: Int, end: Int) = element.execute("$jsExpression.setSelectionRange($start, $end);")

    fun setReadOnly(ro: Boolean) = element.execute("$jsExpression.readOnly = $ro;")
}

enum class InputType {
    button, checkbox, color, date, datetime, email, file, hidden, image, month, number, password, radio, range, reset, search, submit, tel, text, time, url, week
}

fun ElementCreator<Element>.textarea(rows: Int? = null, cols: Int? = null, required: Boolean? = null, attributes: Map<String, Any> = attr): TextAreaElement {
    return TextAreaElement(element("textarea", attributes = attributes
            .set("rows", rows)
            .set("cols", cols)
            .set("required", required)
    ))
}

open class SelectElement(parent: Element) : ValueElement(parent, kvarUpdateEvent = "change")

fun <T : Any> ElementCreator<Element>.select(options: List<T>, optionValue: (T) -> String, optionText: (T) -> String = { it.toString() }, name: String? = null, required: Boolean? = null, attributes: Map<String, Any> = attr): SelectElement {
    return select(options.map {
        optionValue(it) to optionText(it)
    }, name, required, attributes)
}

fun ElementCreator<Element>.select(options: List<Pair<String, Any>>, name: String? = null, required: Boolean? = null, attributes: Map<String, Any> = attr): SelectElement {
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

open class TextAreaElementReader(element: TextAreaElement) : ElementReader(element) {
    val value get() = receiver.evaluate("($jsExpression.innerText);")
}


fun ElementCreator<Element>.label(forId: String?, attributes: Map<String, Any> = attr) = LabelElement(element("label", attributes = attributes.set("for", forId)))

fun ElementCreator<Element>.label(for_: Element? = null, attributes: Map<String, Any> = attr) = LabelElement(element("label", attributes = attributes.set("for", for_?.id)))

open class LabelElement(wrapped: Element) : Element(wrapped)

/**
 * Abstract class for the various elements that have a `value` attribute and which support `change` and `input` events.
 */
abstract class ValueElement(open val element: Element, val kvarUpdateEvent: String = "input") : Element(element) {
    fun getValue(): CompletableFuture<String> = element.evaluate("$jsExpression.value;") { it.toString() }
            ?: error("Not sure why .evaluate() would return null")

    fun setValue(newValue: String) = element.browser.execute("$jsExpression.value=${newValue.toJson()};")
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

    private @Volatile
    var _valueKvar: KVar<String>? = null

    var value: KVar<String>
        get() {
            if (_valueKvar == null) {
                _valueKvar = KVar("")
            }
            return _valueKvar!!
        }
        set(v) {
            if (_valueKvar != null) error("`value` may only be set once, and cannot be set after it has been retrieved")
            setValue(v, updateOn = kvarUpdateEvent)
            _valueKvar = v
        }

    /**
     * Automatically update `toBind` with the value of this INPUT element when `updateOn` event occurs.
     */
    fun setValue(toBind: KVar<String>, updateOn: String = "input") {
        setValue(toBind as KVal<String>)

        // TODO: Would be really nice if it just did a diff on the value and sent that, rather than the
        //       entire value each time PARTICULARLY for large inputs
        on(retrieveJs = "${jsExpression}.value").event(updateOn, ONReceiver.Event::class) {
            toBind.value = it.retrieved ?: error("No value was retrieved")
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
                throw kotlin.IndexOutOfBoundsException("Index $pos out of bounds in list $from")
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

fun <A, B> Pair<KVar<A>, KVar<B>>.combine(): KVar<Pair<A, B>> {
    val newKVar = KVar(this.first.value to this.second.value)
    this.first.addListener { _, n -> newKVar.value = n to this.second.value }
    this.second.addListener { _, n -> newKVar.value = this.first.value to n }

    newKVar.addListener { o, n ->
        this.first.value = n.first
        this.second.value = n.second
    }
    return newKVar
}

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
fun <T : Any> ElementCreator<*>.renderEach(list : KVar<List<T>>, block : ElementCreator<Element>.(value : KVar<T>) -> Unit) {
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
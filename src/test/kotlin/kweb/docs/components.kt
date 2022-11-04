package kweb.docs

import kweb.ElementCreator
import kweb.*
import kweb.InputType.text
import kweb.docs.BulmaInput.BulmaColor
import kweb.docs.BulmaInput.BulmaColor.SUCCESS
import kweb.docs.BulmaInput.BulmaColor.WARNING
import kweb.state.*
import kweb.util.json

// ANCHOR: simple_component
class SimpleComponent(
    val prompt: String = "Enter Your Name",
    val name: KVar<String>
) : Component {
    override fun render(elementCreator: ElementCreator<Element>) {
        with(elementCreator) {
            div {
                h1().text(prompt)
                input(type = text).value = name
            }
            div {
                span().text(name.map { "Hello, $it" })
            }
        }
    }
}
// ANCHOR_END: simple_component

fun simple_component() {
    // ANCHOR: component_usage
Kweb(port = 16097) {
    doc.body {
        render(SimpleComponent(name = kvar("World")))
    }
}
    // ANCHOR_END: component_usage
}


// ANCHOR: bulma_component_example
class BulmaInput(
    val type : InputType,
    val color: KVal<BulmaColor>? = null,
    val size: KVal<BulmaSize>? = null,
    val style: KVal<BulmaStyle>? = null,
    val state: KVal<BulmaState>? = null,
    val disabled: KVal<Boolean>? = null,
    val value: KVar<String>
) : Component {

    override fun render(elementCreator: ElementCreator<*>) {
        with(elementCreator) {
            input(type = type) { inputElement ->
                var inputClassList: KVal<List<String>> = kval(listOf("input"))
                with(inputElement) {

                    if (color != null) {
                        inputClassList += color.map { listOf(it.cssClassName) }
                    }
                    if (size != null) {
                        inputClassList += size.map { listOf(it.cssClassName) }
                    }
                    if (this@BulmaInput.style != null) {
                        inputClassList += this@BulmaInput.style.map { listOf(it.cssClassName) }
                    }
                    if (state != null) {
                        inputClassList += state.map { listOf(it.cssClassName) }
                    }

                    if (disabled != null) {
                        this["disabled"] = disabled.map { it.json }
                    }

                    classes(inputClassList.map { it.joinToString(" ") })

                    this.value = this@BulmaInput.value

                }
            }
        }
    }

    enum class BulmaColor(val cssClassName: String) {
        PRIMARY("is-primary"),
        LINK("is-link"),
        INFO("is-info"),
        SUCCESS("is-success"),
        WARNING("is-warning"),
        DANGER("is-danger")
    }

    enum class BulmaSize(val cssClassName: String) {
        SMALL("is-small"),
        NORMAL("is-normal"),
        MEDIUM("is-medium"),
        LARGE("is-large")
    }

    enum class BulmaStyle(val cssClassName: String) {
        ROUNDED("is-rounded"),
        FOCUSED("is-focused")
    }

    enum class BulmaState(val cssClassName: String) {
        NORMAL("is-normal"),
        HOVER("is-hovered"),
        FOCUS("is-focused"),
        LOADING("is-loading"),
    }
}
// ANCHOR_END: bulma_component_example

fun bulmaComponentUsageExample() {
    // ANCHOR: bulma_component_usage
Kweb(port = 12354) {
    doc.head {
        element("link",
            attributes = mapOf(
                "rel" to "stylesheet".json,
                "href" to "https://cdnjs.cloudflare.com/ajax/libs/bulma/0.7.1/css/bulma.min.css".json
            )
        )
    }

    doc.body {
        val username = kvar("")
        val color = username.map { if (it.length < 5) { WARNING } else { SUCCESS }
        }
        render(BulmaInput(type = text, value = username, color = color))
    }
}
    // ANCHOR_END: bulma_component_usage
}
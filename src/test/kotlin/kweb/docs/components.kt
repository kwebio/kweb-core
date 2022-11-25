package kweb.docs

import kweb.*
import kweb.InputType.text
import kweb.components.Component
import kweb.state.*
import kweb.util.json

// ANCHOR: simple_component
fun Component.simple(
    prompt: String = "Enter Your Name",
    name: KVar<String>
) {
    div {
        h1().text(prompt)
        input(type = text).value = name
    }
    div {
        span().text(name.map { "Hello, $it" })
    }
}
// ANCHOR_END: simple_component

fun simple_component() {
    // ANCHOR: component_usage
    Kweb(port = 16097) {
        doc.body {
            simple(name = kvar("World"))
        }
    }
    // ANCHOR_END: component_usage
}


// ANCHOR: bulma_component_example
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

fun Component.bulmaInput(
    type: InputType,
    color: KVal<BulmaColor>? = null,
    size: KVal<BulmaSize>? = null,
    style: KVal<BulmaStyle>? = null,
    state: KVal<BulmaState>? = null,
    disabled: KVal<Boolean>? = null,
    value: KVar<String>
) {
    input(type = type) { inputElement ->
        var inputClassList: KVal<List<String>> = kval(listOf("input"))
        with(inputElement) {

            if (color != null) {
                inputClassList += color.map { listOf(it.cssClassName) }
            }
            if (size != null) {
                inputClassList += size.map { listOf(it.cssClassName) }
            }
            if (style != null) {
                inputClassList += style.map { listOf(it.cssClassName) }
            }
            if (state != null) {
                inputClassList += state.map { listOf(it.cssClassName) }
            }

            if (disabled != null) {
                this["disabled"] = disabled.map { it.json }
            }

            classes(inputClassList.map { it.joinToString(" ") })

            this.value = value

        }
    }
}
// ANCHOR_END: bulma_component_example

fun bulmaComponentUsageExample() {
    // ANCHOR: bulma_component_usage
    Kweb(port = 12354) {
        doc.head {
            element(
                "link",
                attributes = mapOf(
                    "rel" to "stylesheet".json,
                    "href" to "https://cdnjs.cloudflare.com/ajax/libs/bulma/0.7.1/css/bulma.min.css".json
                )
            )
        }

        doc.body {
            val username = kvar("")
            val color = username.map { if (it.length < 5) BulmaColor.WARNING else BulmaColor.SUCCESS }
            bulmaInput(type = text, value = username, color = color)
        }
    }
    // ANCHOR_END: bulma_component_usage
}
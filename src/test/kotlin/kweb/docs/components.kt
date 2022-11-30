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
interface BulmaClass {
    val cssClassName: String
}

enum class BulmaColor(override val cssClassName: String) : BulmaClass {
    PRIMARY("is-primary"), LINK("is-link"),
    INFO("is-info"), SUCCESS("is-success"),
    WARNING("is-warning"), DANGER("is-danger")
}

enum class BulmaSize(override val cssClassName: String) : BulmaClass {
    SMALL("is-small"), NORMAL("is-normal"),
    MEDIUM("is-medium"), LARGE("is-large")
}

enum class BulmaStyle(override val cssClassName: String) : BulmaClass {
    ROUNDED("is-rounded"),
}

enum class BulmaState(override val cssClassName: String) : BulmaClass {
    NORMAL("is-normal"), HOVER("is-hovered"),
    FOCUS("is-focused"), LOADING("is-loading"),
}

fun Component.bulmaInput(
    type: InputType,
    value: KVar<String>,
    vararg classes : KVal<out BulmaClass> = arrayOf(),

) {
    input(type = type) { inputElement ->
        var inputClassList: KVal<List<String>> = kval(listOf("input"))

        for (c in classes) {
            inputClassList += c.map { listOf(it.cssClassName) }
        }

        with(inputElement) {

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
        bulmaInput(type = text, value = username, color)
    }
}
    // ANCHOR_END: bulma_component_usage
}
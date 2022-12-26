package kweb
import kweb.components.Component
import kweb.state.KVar

fun main() {
    Kweb(
        port = 2395,
        plugins = listOf(
        )
    ) {
        doc.body {
            val state1 = kvar("foo")
            div().new {
                textEditor(state1)
            }

            div().new {
                with(button()) {
                    text("reset")
                    on.click {
                        state1.value = ""
                    }
                }
            }

            div().new {
                with(button()) {
                    text("print")
                    on.click {
                        println("value1 =${state1.value}")
                    }
                }

            }
        }
    }
}


fun Component.textEditor(
    state: KVar<String>,
) {
    val inputElement = input()
    inputElement.value = state
}

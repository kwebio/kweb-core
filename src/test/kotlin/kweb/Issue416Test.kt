package kweb

fun main() {
    Kweb(
        port = 2395
    ) {
        doc.body {
            val state = kvar("foo")
            div().new {

                val inputElement = input(
                    type = InputType.text,
                )
                inputElement.value = state
            }

            div().new {
                with(button()) {
                    text("reset")
                    on.click {
                        state.value = "foo"
                    }
                }
            }

            div().new {
                with(button()) {
                    text("print")
                    on.click {
                        println("value=${state.value}")
                    }
                }

            }
        }
    }

}
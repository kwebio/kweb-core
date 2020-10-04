package kweb

import kweb.state.KVar

fun main() {
    Kweb(port = 5415) {
        doc.body.new {
            val counter = KVar(1)
            val imageString = counter.map { "$it.img" }
            img().setAttribute("src", imageString)
            button().let { button ->
                button.text("Increment")
                button.on.click {
                    counter.value++
                }
            }
        }
    }
}
import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe

object KwebSpec : Spek({
    describe("Start a kweb instance") {
        beforeGroup {
            val kweb = Kweb(port = 12243) {
                doc.body.new {
                    h1().text("Lorum Ipsum")
                }
            }

        }

    }
})


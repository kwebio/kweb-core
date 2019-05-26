import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

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


package kweb.routing

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class UrlToPathSegmentsRFSpec : FreeSpec() {
    init {
        "Test translation and reverse translation" - {
            "Simple URL without query fragment" {
                val orig = "/one/two/three"
                val trans = UrlToPathSegmentsRF.invoke(orig)
                trans shouldBe listOf("one", "two", "three")
                val orig2 = UrlToPathSegmentsRF.reverse("/four/five?seven", trans)
                orig2 shouldBe "/one/two/three?seven"
            }
        }
    }
}

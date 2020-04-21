package kweb.routing

import io.kotlintest.specs.FreeSpec
import org.amshove.kluent.shouldBeEqualTo

class UrlToPathSegmentsRFSpec : FreeSpec() {
    init {
        "Test translation and reverse translation" - {
            "Simple URL without query fragment" {
                val orig = "/one/two/three"
                val trans = UrlToPathSegmentsRF.invoke(orig)
                trans shouldBeEqualTo  listOf("one", "two", "three")
                val orig2 = UrlToPathSegmentsRF.reverse("/four/five?seven", trans)
                orig2 shouldBeEqualTo "/one/two/three?seven"
            }
        }
    }
}
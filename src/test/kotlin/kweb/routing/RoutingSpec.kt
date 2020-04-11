package kweb.routing

import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import kweb.state.KVar
import kweb.subList

/**
 * Created by ian on 4/30/17.
 */

class RoutingSpec : FreeSpec({
    "a simple KVar list where a subList is created" - {
        val list = KVar(listOf(1, 2, 3, 4))
        val sublist = list.subList(1, 3)
        "should be the correct subList" {
            sublist.value shouldBe listOf(2, 3)
        }
    }
    "a simple KVar list where a subList is created and modified" - {
        val list = KVar(listOf(1, 2, 3, 4))
        val sublist = list.subList(1, 3)
        sublist.value = listOf(8, 9, 10)
        "should be the correct subList" {
            list.value shouldBe listOf(1, 8, 9, 10, 4)

        }
    }

    "Path segment transform works" - {
        val url1 = "/p1/p2?query#fragment"
        val segments = UrlToPathSegmentsRF.invoke(url1)
        "should extract segments correctly" {
            segments shouldBe listOf("p1", "p2")
        }

        "should replace segments correctly" {
            val url2 = "/foo/bar?query#fragment"
            val replaced = UrlToPathSegmentsRF.reverse(url2, listOf("a", "b", "c"))
            replaced shouldBe "/a/b/c?query#fragment"
        }
    }
})

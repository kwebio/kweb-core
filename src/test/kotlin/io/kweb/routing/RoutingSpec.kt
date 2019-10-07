package io.kweb.routing

import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.kweb.state.KVar
import io.kweb.state.subList

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
})

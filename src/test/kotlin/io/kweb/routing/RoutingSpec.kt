package io.kweb.routing

import io.kweb.state.*
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

/**
 * Created by ian on 4/30/17.
 */

object RoutingSpec : Spek({
    given("a simple KVar list where a subList is created") {
        val list = KVar(listOf(1, 2, 3, 4))
        val sublist = list.subList(1, 3)
        it("should be the correct subList") {
            sublist.value.shouldEqual(listOf(2, 3))
        }
    }
    given("a simple KVar list where a subList is created and modified") {
        val list = KVar(listOf(1, 2, 3, 4))
        val sublist = list.subList(1, 3)
        sublist.value = listOf(8, 9, 10)
        it("should be the correct subList") {
            list.value.shouldEqual(listOf(1, 8, 9, 10, 4))
        }
    }
})

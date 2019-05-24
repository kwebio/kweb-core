package io.kweb.routing

import io.kweb.state.*
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

/**
 * Created by ian on 4/30/17.
 */

object RoutingSpec : Spek({
    Feature("kvar list") {
        Scenario("a simple KVar list where a subList is created") {
            val list = KVar(listOf(1, 2, 3, 4))
            val sublist = list.subList(1, 3)
            Then("should be the correct subList") {
                sublist.value.shouldEqual(listOf(2, 3))
            }
        }
        Scenario("a simple KVar list where a subList is created and modified") {
            val list = KVar(listOf(1, 2, 3, 4))
            val sublist = list.subList(1, 3)
            When("sublist is modified"){
                sublist.value = listOf(8, 9, 10)
            }
            Then("should be the correct subList") {
                list.value.shouldEqual(listOf(1, 8, 9, 10, 4))
            }
        }
    }
})

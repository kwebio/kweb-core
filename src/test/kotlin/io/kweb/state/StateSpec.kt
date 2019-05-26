package io.kweb.state

import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import org.spekframework.spek2.style.specification.describe

/**
 * Created by ian on 6/18/17.
 */
object StateSpec : Spek({
    describe("KVal") {
        val readOnlyBindable = KVal("Test")
        context("retrieving its value") {
            val value = readOnlyBindable.value
            it("should match the value it was initialized with") {
                readOnlyBindable.value shouldEqual "Test"
            }
        }
    }

    Feature("A simple string KVar") {
        val kvar = KVar("Foo")
        var old: String? = null
        var new: String? = null
        val handle = kvar.addListener { o, n ->
            old shouldEqual null
            new shouldEqual null
            old = o
            new = n
        }
        Scenario("KVar modified with listener") {
            When("value is modified") {
                kvar.value = "Bar"
            }
            Then("should call the listener, modifying the vars accordingly") {
                old shouldEqual "Foo"
                new shouldEqual "Bar"
            }
        }
        Scenario("removing the listener and modifying the value again") {
            Given("Listener is removed"){
                kvar.removeListener(handle)
            }
            When("value is modified"){
                kvar.value = "FooBar"
            }
            Then("listener shouldn't have been called") {
                old shouldEqual "Foo"
                new shouldEqual "Bar"
            }
        }

        Scenario("creating a one-way mapping") {
            val mappedBindable = kvar.map { it.length }

            When("original KVar is modified") {
                kvar.value = "elephant"
            }
            Then("should be mapped correctly") {
                mappedBindable.value shouldEqual 8
            }
        }
    }

    Feature("Two-way mapping on KVar") {
        val lowerCaseVar = KVar("foo")
        val upperCaseVar = lowerCaseVar.map(object : ReversableFunction<String, String>("upperCase") {
            override fun invoke(from: String) = from.toUpperCase()

            override fun reverse(original: String, change: String) = change.toLowerCase()

        })
        Scenario("Mapping from original to target") {

            When("original KVar is modified") {
                lowerCaseVar.value = "one"
            }
            Then("value should be mapped correctly to target"){
                val value = upperCaseVar.value
                value shouldEqual "ONE"
            }
        }
        Scenario("Mapping from target to original"){
            When("target KVar is modified"){
                upperCaseVar.value = "TWO"
            }
            Then("value should be mapped correctly to original"){
                val value = lowerCaseVar.value
                value shouldEqual "two"
            }
        }
    }
})

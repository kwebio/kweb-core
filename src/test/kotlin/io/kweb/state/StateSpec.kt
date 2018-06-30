package io.kweb.state

import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

/**
 * Created by ian on 6/18/17.
 */
object StateSpec : Spek({
    describe("KVal") {
        val readOnlyBindable = KVal("Test")
        on("retrieving its value") {
            val value = readOnlyBindable.value
            it("should match the value it was initialized with") {
                readOnlyBindable.value shouldEqual "Test"
            }
        }
    }

    describe("A simple string KVar") {
        val kvar = KVar("Foo")
        context("adding a listener that modifies two vars") {
            var old: String? = null
            var new: String? = null
            val handle = kvar.addListener { o, n ->
                old shouldEqual null
                new shouldEqual null
                old = o
                new = n
            }
            on("modifying the value") {
                kvar.value = "Bar"
                it("should call the listener, modifying the vars accordingly") {
                    old shouldEqual "Foo"
                    new shouldEqual "Bar"
                }
            }
            on("removing the listener and modifying the value again") {
                kvar.removeListener(handle)
                kvar.value = "FooBar"
                it("shouldn't call the listener again") {
                    old shouldEqual "Foo"
                    new shouldEqual "Bar"
                }

            }
        }
        context("creating a one-way mapping") {
            val mappedBindable = kvar.map { it.length }
            on("modifying the original KVar") {
                kvar.value = "elephant"
                it("should be mapped correctly") {
                    mappedBindable.value shouldEqual 8
                }

            }
        }

    }
    describe("a string KVar") {
        val lowerCaseVar = KVar("foo")
        context("creating a two-way mapping") {
            val upperCaseVar = lowerCaseVar.map(object : ReversableFunction<String, String>("upperCase") {
                override fun invoke(from: String) = from.toUpperCase()

                override fun reverse(original: String, change: String) = change.toLowerCase()

            })
            on("modifying the original KVar") {
                lowerCaseVar.value = "one"
                it("should be mapped correctly") {
                    val value = upperCaseVar.value
                    value shouldEqual "ONE"
                }
            }
            on("modifying the mapped Kvar") {
                upperCaseVar.value = "TWO"
                it("should be unmapped correctly") {
                    val value = lowerCaseVar.value
                    value shouldEqual "two"
                }
            }
        }
    }
})

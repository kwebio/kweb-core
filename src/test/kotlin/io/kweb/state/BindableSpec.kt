package io.kweb.state

import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

/**
 * Created by ian on 6/18/17.
 */
object BindableSpec : Spek({
    describe("ReadOnlyBindable") {
        val readOnlyBindable = ReadOnlyBindable("Test")
        on("retrieving its value") {
            val value = readOnlyBindable.value
            it("should match the value it was initialized with") {
                readOnlyBindable.value shouldEqual "Test"
            }
        }
    }

    describe("A simple string bindable") {
        val bindable = Bindable("Foo")
        context("adding a listener that modifies two vars") {
            var old: String? = null
            var new: String? = null
            val handle = bindable.addListener { o, n ->
                old shouldEqual null
                new shouldEqual null
                old = o
                new = n
            }
            on("modifying the value") {
                bindable.value = "Bar"
                it("should call the listener, modifying the vars accordingly") {
                    old shouldEqual "Foo"
                    new shouldEqual "Bar"
                }
            }
            on("removing the listener and modifying the value again") {
                bindable.removeListener(handle)
                bindable.value = "FooBar"
                it("shouldn't call the listener again") {
                    old shouldEqual "Foo"
                    new shouldEqual "Bar"
                }

            }
        }
        context("creating a one-way mapping") {
            val mappedBindable = bindable.map { it.length }
            on("modifying the original bindable") {
                bindable.value = "elephant"
                it("should be mapped correctly") {
                    mappedBindable.value shouldEqual 8
                }

            }
        }

    }
    describe("a string bindable") {
        val lowerCaseBindable = Bindable("foo")
        context("creating a two-way mapping") {
            val upperCaseBindable = lowerCaseBindable.map(object : ReversableFunction<String, String> {
                override fun map(from: String) = from.toUpperCase()

                override fun unmap(original: String, change: String) = change.toLowerCase()

            })
            on("modifying the original bindable") {
                lowerCaseBindable.value = "one"
                it("should be mapped correctly") {
                    val value = upperCaseBindable.value
                    value shouldEqual "ONE"
                }
            }
            on("modifying the mappedBindable") {
                upperCaseBindable.value = "TWO"
                it("should be unmapped correctly") {
                    val value = lowerCaseBindable.value
                    value shouldEqual "two"
                }
            }
        }
    }
})

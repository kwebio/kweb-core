package io.kweb.state

import io.kotlintest.specs.FreeSpec
import org.amshove.kluent.shouldBeEqualTo

/**
 * Created by ian on 6/18/17.
 */
class StateSpec : FreeSpec({
    "KVal" - {
        val readOnlyBindable = KVal("Test")
        "retrieving its value" - {
            val value = readOnlyBindable.value
            "should match the value it was initialized with" {
                readOnlyBindable.value shouldBeEqualTo "Test"
            }
        }
    }

    "A simple string KVar" - {
        val kvar = KVar("Foo")
        var old: String? = null
        var new: String? = null
        val handle = kvar.addListener { o, n ->
            old shouldBeEqualTo null
            new shouldBeEqualTo null
            old = o
            new = n
        }
        "KVar modified with listener" - {
            "value is modified" {
                kvar.value = "Bar"
            }
            "should call the listener, modifying the vars accordingly" {
                old shouldBeEqualTo "Foo"
                new shouldBeEqualTo "Bar"
            }
        }
        "removing the listener and modifying the value again" - {
            "Listener is removed" - {
                kvar.removeListener(handle)
            }
            "value is modified" - {
                kvar.value = "FooBar"
            }
            "listener shouldn't have been called" {
                old shouldBeEqualTo "Foo"
                new shouldBeEqualTo "Bar"
            }
        }

        "creating a one-way mapping" - {
            val mappedBindable = kvar.map { it.length }

            "original KVar is modified" {
                kvar.value = "elephant"
            }
            "should be mapped correctly" {
                mappedBindable.value shouldBeEqualTo 8
            }
        }
    }

    "Two-way mapping on KVar" - {
        val lowerCaseVar = KVar("foo")
        val upperCaseVar = lowerCaseVar.map(object : ReversibleFunction<String, String>("upperCase") {
            override fun invoke(from: String) = from.toUpperCase()

            override fun reverse(original: String, change: String) = change.toLowerCase()

        })
        "Mapping from original to target" - {

            "original KVar is modified" {
                lowerCaseVar.value = "one"
            }
            "value should be mapped correctly to target" {
                val value = upperCaseVar.value
                value shouldBeEqualTo "ONE"
            }
        }
        "Mapping from target to original" - {
            "target KVar is modified" {
                upperCaseVar.value = "TWO"
            }
            "value should be mapped correctly to original" {
                val value = lowerCaseVar.value
                value shouldBeEqualTo "two"
            }
        }
    }
})

package io.kweb.state

import mu.KotlinLogging

/**
 * Created by ian on 4/3/17.
 */

/**
 * Stores a value which can change, and allows listeners to be attached which will be called if/when the
 * value changes.
 *
 * @sample state_sample
 */

private val logger = KotlinLogging.logger {}

abstract class ReversableFunction<Input, Output>(val label : String) {
    abstract operator fun invoke(from : Input) : Output

    abstract fun reverse(original : Input, change : Output) : Input
}

fun state_sample() {
    val obs = KVar("Hello")
    val handle = obs.addListener { old, new ->
        println("obs changed to $old to $new")
    }
    obs.value = "Goodbye" // Will print "obs changed to Hello to Goodbye"
    obs.removeListener(handle)
    obs.value = "Hello"   // Nothing will be printed because listener has been removed
}
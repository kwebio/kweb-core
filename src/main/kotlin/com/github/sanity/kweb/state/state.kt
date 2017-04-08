package com.github.sanity.kweb.state

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.ElementCreator
import com.github.sanity.kweb.dom.element.modification.delete
import com.github.sanity.kweb.random
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by ian on 4/3/17.
 */

/**
 * Stores a value which can change, and allows listeners to be attached which will be called if/when the
 * value changes.
 *
 * @sample observable_sample
 */
class Observable<T : Any?>(@Volatile private var state : T) {
    private val listeners = ConcurrentHashMap<Long, (T, T) -> Unit>()

    fun addListener(listener : (T, T) -> Unit) : Long {
        val handle = random.nextLong()
        listeners[handle] = listener
        return handle
    }

    fun removeListener(handle : Long) = listeners.remove(handle)

    val value get() = state

    @Synchronized fun changeTo(newVal : T) {
        listeners.values.forEach {it(state, newVal)}
        state = newVal
    }
}

fun <E : Element> ElementCreator<E>.bind() = RenderReceiver<E>(this)

class RenderReceiver<out E : Element>(private val ec : ElementCreator<E>) {
    fun <T : Any> to(observable: Observable<T>, d : ElementCreator<E>.(T) -> Unit) {
        val previousChildren = ArrayList<Element>()
        val newChildHandle = ec.addNewChildListener { previousChildren += it }
        d(ec, observable.value)
        ec.removeNewChildListener(newChildHandle)
        observable.addListener({ oldState, newState ->
            if (oldState != newState) {
                previousChildren.forEach({it.delete()})
                previousChildren.clear()
                val newChildHandle = ec.addNewChildListener { previousChildren += it }
                d(ec, newState)
                ec.removeNewChildListener(newChildHandle)
            }
        })
    }
}

fun observable_sample() {
    val obs = Observable("Hello")
    val handle = obs.addListener( {old, new ->
        println("obs changed to $old to $new")
    })
    obs.changeTo("Goodbye") // Will print "obs changed to Hello to Goodbye"
    obs.removeListener(handle)
}
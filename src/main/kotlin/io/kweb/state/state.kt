package io.kweb.state

import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.Cleaner
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.modification.text
import io.kweb.random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.properties.Delegates.notNull

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
        if (newVal != state) {
            listeners.values.forEach { it(state, newVal) }
            state = newVal
        }
    }

    fun <O> view(mapper : (T) -> O, unmapper : ((T, O) -> T)? = null) : Observable<O> {
        val oobs = Observable(mapper(value))
        addListener { old, new ->
            oobs.changeTo(mapper(new))
        }
        if (unmapper != null) {
            oobs.addListener({ old, new ->
                changeTo(unmapper(value, new))
            })
        }
        return oobs
    }
}

fun Element.text(oText : Observable<String>) {
    text(oText.value)
    oText.addListener{ old, new ->
        text(new)
    }
}

val <E : Element> ElementCreator<E>.bind get() = RenderReceiver<E>(this)

class RenderReceiver<out E : Element>(private val ec : ElementCreator<E>) {
    fun <T : Any> to(observable: Observable<T>, d : ElementCreator<E>.(T) -> Unit) {
        // TODO: We have to use the notNull() delegate because listenerId isn't known
        // TODO: until after the call to addListener()
        // TODO: This is ugly, perhaps listenerId should be passed as param to listener
        var listenerId by notNull<Long>()
        val lastRenderCleaners = ConcurrentLinkedDeque<Cleaner>()
        listenerId = observable.addListener({ oldState, newState ->
            if (oldState != newState) {
                while (!lastRenderCleaners.isEmpty()) {
                    lastRenderCleaners.poll().invoke()
                }
                ec.onCleanup(true) {
                    observable.removeListener(listenerId)
                }
                ec.withCleanupListener({lastRenderCleaners.add(it)}) {
                    d(ec, newState)
                }
            }
        })
        ec.withCleanupListener({lastRenderCleaners.add(it)}) {
            d(ec, observable.value)
        }
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
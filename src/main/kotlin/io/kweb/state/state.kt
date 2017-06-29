package io.kweb.state

import io.kweb.random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.properties.Delegates

/**
 * Created by ian on 4/3/17.
 */

/**
 * Stores a value which can change, and allows listeners to be attached which will be called if/when the
 * value changes.
 *
 * @sample watchable_sample
 */

open class ReadOnlyWatchable<T : Any>(initialValue: T) {
    private @Volatile var isClosed = false

    protected val listeners = ConcurrentHashMap<Long, (T, T) -> Unit>()
    private val closeHandlers = ConcurrentLinkedDeque<() -> Unit>()

    fun addListener(listener : (T, T) -> Unit) : Long {
        assertNotClosed()
        val handle = random.nextLong()
        listeners[handle] = listener
        return handle
    }

    private @Volatile var value_: T = initialValue

    open val value : T get() {
        assertNotClosed()
        return value_
    }

    fun removeListener(handle: Long) {
        assertNotClosed()
        listeners.remove(handle)
    }

    fun <O : Any> map(mapper: (T) -> O): ReadOnlyWatchable<O> {
        assertNotClosed()
        val newObservable = ReadOnlyWatchable(mapper(value_))
        addListener { old, new ->
            if (new != value_) {
                val mappedValue = mapper(new)
                newObservable.value_ = mappedValue
                newObservable.listeners.values.forEach { it(mapper(old), mappedValue) }
            }
        }
        return newObservable
    }

    fun close() {
        if (isClosed) {
            throw RuntimeException("Attempted to close but was already closed")
        } else {
            isClosed = true
        }
        closeHandlers.forEach { it.invoke() }
    }

    fun onClose(handler: () -> Unit) {
        assertNotClosed()
        closeHandlers += handler
    }

    internal fun assertNotClosed() {
        if (isClosed) {
            throw RuntimeException("Not permitted after Watchable is closed()")
        }
    }
}

class Watchable<T : Any>(initialValue: T) : ReadOnlyWatchable<T>(initialValue) {
    override var value: T by Delegates.observable(initialValue) { prop, old, new ->
        assertNotClosed()
        if (old != new) {
            listeners.values.forEach { it(old, new) }
        }
    }

    fun <O : Any> map(mapper: (T) -> O, unmapper: ((T, O) -> T)): Watchable<O> {
        assertNotClosed()
        val mappedObservable = Watchable(mapper(value))
        // TODO: Should these be cleaned up somehow, or will JVM garbage collection take care of it?
        addListener { old, new ->
            mappedObservable.value = mapper(new)
        }
        mappedObservable.addListener({ old, new ->
            value = unmapper(value, new)
        })
        return mappedObservable
    }
}

fun watchable_sample() {
    val obs = Watchable("Hello")
    val handle = obs.addListener( {old, new ->
        println("obs changed to $old to $new")
    })
    obs.value = "Goodbye" // Will print "obs changed to Hello to Goodbye"
    obs.removeListener(handle)
    obs.value = "Hello"   // Nothing will be printed because listener has been removed
}
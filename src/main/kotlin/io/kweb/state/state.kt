package io.kweb.state

import io.kweb.random
import mu.KotlinLogging
import java.util.concurrent.*
import kotlin.properties.Delegates

/**
 * Created by ian on 4/3/17.
 */

/**
 * Stores a value which can change, and allows listeners to be attached which will be called if/when the
 * value changes.
 *
 * @sample bindable_sample
 */

private val logger = KotlinLogging.logger {}

open class ReadOnlyBindable<T : Any>(initialValue: T) {
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

    fun <O : Any> map(mapper: (T) -> O): ReadOnlyBindable<O> {
        assertNotClosed()
        val newObservable = ReadOnlyBindable(mapper(value_))
        val handle = addListener { old, new ->
            if (new != value_) {
                val mappedValue = mapper(new)
                newObservable.value_ = mappedValue
                newObservable.listeners.values.forEach { it(mapper(old), mappedValue) }
            }
        }
        newObservable.onClose { removeListener(handle) }
        this.onClose { newObservable.close() }
        return newObservable
    }

    fun close() {
        if (isClosed) {
            logger.warn("Attempted to close but was already closed")
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
            logger.warn("Shouldn't be called after Bindable is closed()")
        }
    }
}

interface ReversableFunction<Input, Output> {
    fun map(from : Input) : Output

    fun unmap(original : Input, change : Output) : Input
}

class Bindable<T : Any>(initialValue: T) : ReadOnlyBindable<T>(initialValue) {
    override var value: T by Delegates.observable(initialValue) { _, old, new ->
        assertNotClosed()
        if (old != new) {
            listeners.values.forEach { it(old, new) }
        }
    }



    fun <O : Any> map(rf : ReversableFunction<T, O>): Bindable<O> {
        assertNotClosed()
        val mappedObservable = Bindable(rf.map(value))
        val myChangeHandle = addListener { _, new ->
            mappedObservable.value = rf.map(new)
        }
        onClose { removeListener(myChangeHandle) }
        val origChangeHandle = mappedObservable.addListener({ _, new ->
            value = rf.unmap(value, new)
        })
        onClose { mappedObservable.removeListener(origChangeHandle)}
        return mappedObservable
    }
}

/*
inline fun <reified T : Any> Shoebox<T>.getAsBindable(key : String) : Bindable<T>? {
    val initialValue = this.get(key)
    return if (initialValue != null) {
        val b = Bindable(initialValue)
        this.onChange(key, {_, new, _ ->
            b.value = new
        })
        b
    } else {
        null
    }
}
*/

fun bindable_sample() {
    val obs = Bindable("Hello")
    val handle = obs.addListener( {old, new ->
        println("obs changed to $old to $new")
    })
    obs.value = "Goodbye" // Will print "obs changed to Hello to Goodbye"
    obs.removeListener(handle)
    obs.value = "Hello"   // Nothing will be printed because listener has been removed
}
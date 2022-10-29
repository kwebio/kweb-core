package kweb.state

import kweb.util.random
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

private val logger = KotlinLogging.logger {}

/**
 * A KVal is a **read-only** observable container for a value of type T. These are typically created by
 * [KVal.map] or [KVar.map], but can also be created directly.
 */
open class KVal<T : Any?>(value: T) : AutoCloseable{

    @Volatile
    protected var closeReason: CloseReason? = null

    internal val isClosed get() = closeReason != null

    protected val listeners = ConcurrentHashMap<Long, (T, T) -> Unit>()
    private val closeHandlers = ConcurrentLinkedDeque<() -> Unit>()

    /**
     * Add a listener to this KVar. The listener will be called whenever the [value] property changes.
     */
    fun addListener(listener: (T, T) -> Unit): Long {
        verifyNotClosed("add a listener")
        val handle = random.nextLong()
        listeners[handle] = listener
        return handle
    }

    @Volatile
    private var pValue: T = value

    /**
     * The current value of this KVal, this can be read but not modified - but it may change if this [KVal] was
     * created by mapping another [KVal] or using [KVal.map]. If you want to modify this value then you should
     * be using a [KVar] instead.
     */
    open val value: T
        get() {
            verifyNotClosed("retrieve KVal.value")
            return pValue
        }

    /**
     * Remove a listener from this KVar.  The listener will no longer be called when the [value] property
     * changes.
     */
    fun removeListener(handle: Long) {
        listeners.remove(handle)
    }

    /**
     * Create another KVal that is a mapping of this KVal.  The mapping function will be called whenever this KVal
     * changes, and the new KVal will be updated with the result of the mapping function.
     *
     * For bi-directional mappings, see [KVar.map].
     */
    fun <O : Any?> map(mapper: (T) -> O): KVal<O> {
        if (isClosed) {
            error("Can't map this var because it was closed due to $closeReason")
        }
        val mappedKVal = KVal(mapper(value))
        val handle = addListener { old, new ->
            if (!isClosed && !mappedKVal.isClosed) {
                if (old != new) {
                    logger.debug("Updating mapped $value to $new")
                    val mappedValue = mapper(new)
                    mappedKVal.pValue = mappedValue
                    mappedKVal.listeners.values.forEach { listener ->
                        try {
                            val mappedOld = mapper(old)
                            if (mappedOld != mappedValue) {
                                listener(mappedOld, mappedValue)
                            }
                        } catch (e: Exception) {
                            mappedKVal.close(CloseReason("Closed because mapper threw an exception", e))
                        }

                    }
                }
            } else {
                error("Not propagating change to mapped variable because this or the other observable are closed, old: $old, new: $new")
            }
        }
        mappedKVal.onClose { removeListener(handle) }
        this.onClose {
            mappedKVal.close(CloseReason("KVar this was mapped from was closed"))
        }
        return mappedKVal
    }

    override fun close() {
        close(CloseReason("Kval.close() was called"))
    }

    /**
     * Close this KVal, and notify all handlers that it has been closed.
     */
    fun close(reason: CloseReason) {
        if (!isClosed) {
            closeReason = reason
            closeHandlers.forEach { it.invoke() }
        }
    }

    /**
     * Add a handler to be called when this KVal is closed.
     */
    fun onClose(handler: () -> Unit) {
        verifyNotClosed("add a close handler")
        closeHandlers += handler
    }

    override fun toString(): String {
        verifyNotClosed("call KVal.toString()")
        return "KVal($value)"
    }

    /**
     * Throw an exception if this KVal is closed.
     */
    protected fun verifyNotClosed(triedTo: String) {
        closeReason.let { closeReason ->
            if (closeReason != null) {
                if (closeReason.cause == null) {
                    throw IllegalStateException("Can't $triedTo as it was closed due to ${closeReason.explanation}")
                } else {
                    throw IllegalStateException("Can't $triedTo as it was closed due to ${closeReason.explanation}", closeReason.cause)
                }
            }
        }
    }

    protected fun finalize() {
        this.close(CloseReason("Garbage Collected"))
    }

}

operator fun <O : Any> KVal<List<O>>.plus(other : KVal<List<O>>) : KVal<List<O>> {
    val newKVar = KVar(this.value + other.value)
    this.addListener { _, new -> newKVar.value = new + other.value }
    other.addListener { _, new -> newKVar.value = this.value + new }
    return newKVar
}

data class CloseReason(val explanation: String, val cause: Throwable? = null)
package io.kweb.state

import io.kweb.random
import mu.KotlinLogging
import java.util.concurrent.*

private val logger = KotlinLogging.logger {}

open class KVal<T : Any?>(value: T) {

    @Volatile
    internal var isClosed = false

    protected val listeners  = ConcurrentHashMap<Long, (T, T) -> Unit>()
    private val closeHandlers = ConcurrentLinkedDeque<() -> Unit>()

    fun addListener(listener : (T, T) -> Unit) : Long {
        val handle = random.nextLong()
        listeners[handle] = listener
        return handle
    }

    @Volatile
    private var pValue: T = value

    open val value : T get() {
        return pValue
    }

    fun removeListener(handle: Long) {
        listeners.remove(handle)
    }

    // TODO: A cachetime could be specified, to limit recalculation, could be quite broadly useful for expensive
    //       mappings
    fun <O : Any?> map(mapper: (T) -> O): KVal<O> {
        if (isClosed) {
            throw IllegalStateException("Mapping an already closed KVar")
        }
        val newObservable = KVal(mapper(pValue))
        val handle = addListener { old, new ->
            if (!isClosed && !newObservable.isClosed) {
                if (new != pValue) {
                    logger.debug("Updating mapped $pValue to $new")
                    val mappedValue = mapper(new)
                    newObservable.pValue = mappedValue
                    newObservable.listeners.values.forEach {
                        try {
                            it(mapper(old), mappedValue)
                        } catch (e: Exception) {
                            logger.warn("Exception thrown by listener", e)
                        }

                    }
                }
            } else {
                logger.warn("Not propagating change to mapped variable because this or the other observable are closed, old: $old, new: $new")
            }
        }
        newObservable.onClose { removeListener(handle) }
        this.onClose {
            newObservable.close()
        }
  //      newObservable.onClose {
  //          this.close()
  //      }
        return newObservable
    }

    // TODO: Temporary for debugging
    private var closedStack: Array<out StackTraceElement>? = null

    fun close() {
        if (isClosed) {
            val firstStackTrace = closedStack!!
            val secondStackTrace = Thread.currentThread().stackTrace
            logger.debug {
                val st = secondStackTrace
                        .filter { it.methodName != "close" }
                        .filterNot { firstStackTrace.contains(it) }
                        .joinToString { it.toString() }
                "Second stack trace:\t$st"
            }
            logger.debug {
                val st = firstStackTrace
                        .filter { it.methodName != "close" }
                        .filterNot { secondStackTrace.contains(it) }
                        .joinToString { it.toString() }
                "First stack trace:\t$st"
            }
        } else {
            isClosed = true
            closedStack = Thread.currentThread().stackTrace
            closeHandlers.forEach { it.invoke() }
        }
    }

    fun onClose(handler: () -> Unit) {
        if (isClosed) {
            //logger.debug("Shouldn't be called after KVar is closed()")
            logger.warn("Adding a closer handler to an already closed KVar", IllegalStateException())
        }
        closeHandlers += handler
    }

    override fun toString(): String {
        return "KVal($value)"
    }

}
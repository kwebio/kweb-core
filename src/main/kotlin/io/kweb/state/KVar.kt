package io.kweb.state

import mu.KotlinLogging
import kotlin.properties.Delegates

private val logger = KotlinLogging.logger {}

class KVar<T : Any?>(initialValue: T) : KVal<T>(initialValue) {
    override var value: T by Delegates.observable(initialValue) { _, old, new ->
        if (old != new) {
            if (isClosed) {
                logger.warn("Modifying a value in a closed KVar", IllegalStateException("Modifying a value in a closed KVar"))
            }
            listeners.values.forEach { v ->
                v(old, new)

            }
        }
    }

    fun <O : Any> map(reversableFunction : ReversableFunction<T, O>): KVar<O> {
        if (isClosed) {
            //logger.debug("Shouldn't be called after KVar is closed()")
            logger.warn("Mapping an already closed KVar", IllegalStateException())
        }
        val mappedObservable = KVar(reversableFunction(value))
        val myChangeHandle = addListener { old, new ->
            if (old != new) {
                mappedObservable.value = reversableFunction.invoke(new)
            }
        }
        onClose { removeListener(myChangeHandle) }
        val origChangeHandle = mappedObservable.addListener { _, new ->
            value = reversableFunction.reverse(value, new)
        }
        onClose { mappedObservable.removeListener(origChangeHandle)}
        return mappedObservable
    }
}
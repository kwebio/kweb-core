package io.kweb.state

import mu.KotlinLogging
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.Delegates
import kotlin.reflect.KProperty1
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions

private val logger = KotlinLogging.logger {}

class KVar<T : Any?>(initialValue: T) : KVal<T>(initialValue) {
    override var value: T by Delegates.observable(initialValue) { _, old, new ->
        if (old != new) {
            if (isClosed) {
                error("Can't modify a value in a closed KVal - $closeReason")
            }
            listeners.values.forEach { listener ->
                try {
                    listener(old, new)
                } catch (e : Exception) {
                    logger.warn("Exception thrown by listener", e)
                }
            }
        }
    }

    fun <O : Any?> map(reversableFunction: ReversableFunction<T, O>): KVar<O> {
        if (isClosed) {
            logger.warn("Mapping an KVar which had been closed due to $closeReason")
        }
        val mappedObservable = KVar(reversableFunction(value))
        val myChangeHandle = addListener { old, new ->
            if (old != new) {
                try {
                    mappedObservable.value = reversableFunction.invoke(new)
                } catch (e : Exception) {
                    mappedObservable.close(CloseReason("Closed because mapper threw an exception", e))
                }
            }
        }
        onClose { removeListener(myChangeHandle) }
        mappedObservable.onClose { removeListener(myChangeHandle) }
        val origChangeHandle = mappedObservable.addListener { _, new ->
            value = reversableFunction.reverse(value, new)
        }
        onClose { mappedObservable.removeListener(origChangeHandle) }
        return mappedObservable
    }

    override fun toString(): String {
        return "KVar($value)"
    }

}

inline fun <O, reified T : Any?> KVar<T>.property(property: KProperty1<T, O>): KVar<O> {
    return this.map(object : ReversableFunction<T, O>("prop: ${property.name}") {

        private val kClass = T::class
        private val copyFunc = kClass.memberFunctions.firstOrNull { it.name == "copy" }
                ?: error("Can't find `copy` function in class ${kClass.simpleName}, are you sure it's a data object?")
        private val instanceParam = copyFunc.instanceParameter
                ?: error("Unable to obtain instanceParam")
        private val fieldParam = copyFunc.parameters.firstOrNull { it.name == property.name }
                ?: error("Unable to identify parameter for ${property.name} in ${kClass.simpleName}.copy() function")

        override fun invoke(from: T): O = property.invoke(from)

        override fun reverse(original: T, change: O): T = copyFunc.callBy(mapOf(instanceParam to original, fieldParam to change)) as T
    })
}

fun <O : Any> KVar<O?>.notNull(default : O? = null, invertDefault : Boolean = true): KVar<O> {
    return this.map(object : ReversableFunction<O?, O>(label = "notNull") {
        override fun invoke(from: O?): O = from ?: default!!

        override fun reverse(original: O?, change: O): O? = if (invertDefault) {
            if (change != default) change else null
        } else change

    })
}

@ExperimentalContracts
fun <T : Any> KVar<T>.modify(f : (T) -> T) {
    contract {
        callsInPlace(f, InvocationKind.EXACTLY_ONCE)
    }
    this.value = f(this.value)
}
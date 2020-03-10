package io.kweb.state

import mu.KotlinLogging
import kotlin.contracts.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

private val logger = KotlinLogging.logger {}

class KVar<T : Any?>(initialValue: T) : KVal<T>(initialValue) {
    override var value: T by Delegates.observable(initialValue) { _, old, new ->
        if (old != new) {
            verifyNotClosed("modify KVar.value")
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
        verifyNotClosed("create a mapping")
        val mappedKVar = KVar(reversableFunction(value))
        val myChangeHandle = addListener { old, new ->
            if (old != new) {
                try {
                    mappedKVar.value = reversableFunction.invoke(new)
                } catch (throwable : Throwable) {
                    mappedKVar.close(CloseReason("Closed because mapper threw an error or exception", throwable))
                }
            }
        }
        onClose { removeListener(myChangeHandle) }
        mappedKVar.onClose { removeListener(myChangeHandle) }
        val origChangeHandle = mappedKVar.addListener { _, new ->
            value = reversableFunction.reverse(value, new)
        }
        onClose { mappedKVar.removeListener(origChangeHandle) }
        return mappedKVar
    }

    override fun toString(): String {
        verifyNotClosed("call KVar.toString()")
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
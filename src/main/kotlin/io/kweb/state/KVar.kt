package io.kweb.state

import mu.KotlinLogging
import kotlin.properties.Delegates
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

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

    fun <O : Any?> map(reversableFunction: ReversableFunction<T, O>): KVar<O> {
        if (isClosed) {
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
                ?: throw RuntimeException("Can't find `copy` function in class ${kClass.simpleName}, are you sure it's a data object?")
        private val instanceParam = copyFunc.instanceParameter
                ?: throw RuntimeException("Unable to obtain instanceParam")
        private val fieldParam = copyFunc.parameters.firstOrNull { it.name == property.name }
                ?: throw RuntimeException("Unable to identify parameter for ${property.name} in ${kClass.simpleName}.copy() function")

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
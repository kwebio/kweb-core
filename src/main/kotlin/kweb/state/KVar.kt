package kweb.state

import mu.two.KotlinLogging
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.Delegates
import kotlin.reflect.KProperty1
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions

private val logger = KotlinLogging.logger {}

/**
 * A KVar is an observable container for a value of type T.  It must be initialized with [initialValue], and
 * this can then be modified by setting the [KVar.value] property. Listeners may be added using
 * [KVar.addListener], and these will be called whenever the value is changed.
 *
 * From within Kweb's DSL, you can use the [ElementCreator.kvar] function to create a KVar without needing
 * to import KVar, and which will also call [KVar.close] when this part of the DOM is cleaned up.
 */
class KVar<T : Any?>(initialValue: T) : KVal<T>(initialValue) {

    /**
     * The current value of this KVar.  Setting this property to a different value will notify
     * all listeners, but if the new value is the same as the old value then it will be ignored.
     */
    override var value: T by Delegates.observable(initialValue) { _, old, new ->
        if (old != new) {
            verifyNotClosed("modify KVar.value")
            listeners.asMap().values.forEach { listener ->
                try {
                    listener(old, new)
                } catch (e: Exception) {
                    logger.warn("Exception thrown by listener", e)
                }
            }
        }
    }

    /**
     * Create another KVar that is a bi-directional mapping of this KVar.  [ReversibleFunction.invoke] will be called
     * whenever this KVar changes, and the new KVar will be updated with the result of this mapping function.
     *
     * Similarly, if the other KVar is modified then this KVar will be updated with the result of the
     * [ReversibleFunction.reverse] function.
     */
    fun <O : Any?> map(reversibleFunction: ReversibleFunction<T, O>): KVar<O> {
        verifyNotClosed("create a mapping")
        val mappedKVar = KVar(reversibleFunction(value))
        val myChangeHandle = addListener { old, new ->
            if (old != new) {
                try {
                    mappedKVar.value = reversibleFunction.invoke(new)
                } catch (throwable: Throwable) {
                    mappedKVar.close(CloseReason("Closed because mapper threw an error or exception", throwable))
                }
            }
        }
        onClose { removeListener(myChangeHandle) }
        mappedKVar.onClose { removeListener(myChangeHandle) }
        val origChangeHandle = mappedKVar.addListener { _, new ->
            value = reversibleFunction.reverse(value, new)
        }
        onClose { mappedKVar.removeListener(origChangeHandle) }
        return mappedKVar
    }

    override fun toString(): String {
        verifyNotClosed("call KVar.toString()")
        return "KVar($value)"
    }

}

/**
 * Use reflection to create a [KVar] that bi-directionally maps to a mutable property of an
 * object.
 */
inline fun <O, reified T : Any?> KVar<T>.property(property: KProperty1<T, O>): KVar<O> {
    return this.map(object : ReversibleFunction<T, O>("prop: ${property.name}") {

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

/**
 * Bi-directionally map a [KVar] with nullable type to its non-nullable equivalent.
 */
fun <O : Any> KVar<O?>.notNull(default: O? = null, invertDefault: Boolean = true): KVar<O> {
    return this.map(object : ReversibleFunction<O?, O>(label = "notNull") {
        override fun invoke(from: O?): O = from ?: default!!

        override fun reverse(original: O?, change: O): O? = if (invertDefault) {
            if (change != default) change else null
        } else change

    })
}

fun <A, B> Pair<KVar<A>, KVar<B>>.combine(): KVar<Pair<A, B>> {
    val newKVar = KVar(this.first.value to this.second.value)
    val listener1 = this.first.addListener { _, n -> newKVar.value = n to this.second.value }
    val listener2 = this.second.addListener { _, n -> newKVar.value = this.first.value to n }

    newKVar.addListener { _, n ->
        this.first.value = n.first
        this.second.value = n.second
    }

    this.first.onClose {
        newKVar.close(CloseReason("Closed because first KVar was closed"))
    }

    this.second.onClose {
        newKVar.close(CloseReason("Closed because second KVar was closed"))
    }

    newKVar.onClose {
        this.first.removeListener(listener1)
        this.second.removeListener(listener2)
    }

    return newKVar
}


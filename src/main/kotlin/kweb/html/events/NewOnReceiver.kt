package kweb.html.events

import com.github.salomonbrys.kotson.fromJson
import kweb.dom.element.events.ONReceiver
import kweb.gson
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

class NewOnReceiver<T : EventGenerator<T>>(private val source: T, private val retrieveJs: String? = null) {

    val logger = KotlinLogging.logger {}

    fun event(eventName: String, returnEventFields: Set<String> = emptySet(), callback: (event: String) -> Unit): T {
        source.addEventListener(eventName, returnEventFields = returnEventFields, callback = { callback(it.toString()) }, retrieveJs = retrieveJs)
        return source
    }

    inline fun <reified U : Any> event(eventName: String, eventType: KClass<U>, crossinline callback: (event: U) -> Unit): T {
        // TODO: Should probably cache this rather than do the reflection every time
        val eventPropertyNames = memberProperties(eventType)
        return event(eventName, eventPropertyNames) { propertiesAsString ->
            val props: U = gson.fromJson(propertiesAsString)
            try {
                callback(props)
            } catch (e: Exception) {
                logger.error(e) { "Exception thrown by callback in response to $eventName event" }
            }
        }
    }

    companion object {
        val memberPropertiesCache: ConcurrentHashMap<KClass<*>, Set<String>> = ConcurrentHashMap()
        inline fun <reified T : Any> memberProperties(clazz: KClass<T>) =
                memberPropertiesCache.get(clazz)
                        ?: T::class.memberProperties.map { it.name }.toSet().also { memberPropertiesCache.put(clazz, it) }
    }
}
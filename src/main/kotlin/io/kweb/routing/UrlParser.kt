package io.kweb.routing

/**
 * Created by ian on 4/13/17.
 */
import io.kweb.routing.ParsingResult.NoValue
import io.kweb.routing.ParsingResult.ValueExtracted
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaType

inline fun <reified T : Any> parse(url: String,
                                   noinline contextProvider: (KClass<*>)-> Set<KClass<out Any>>): T? {
    val parts = url.trim('/').split('/').filter { it.isNotEmpty() }

    return buildEntity(parts, T::class, contextProvider) { kClass, entityName ->
        val realEntityName = entityName ?: "root"
        val name = kClass.simpleName?.toLowerCase()
        name == realEntityName
    }
}

data class UrlParseException(val reason : String) : Exception()

// TODO This is not optimal because a child of a sealed class no longer need to be nested
fun <T : Any> KClass<T>.meAndNested() = setOf(this, *nestedClasses.toTypedArray())

class ClasspathScanner(vararg val packages: String) {
    val config = ConfigurationBuilder().apply {
        packages.map { name ->
            urls += ClasspathHelper.forPackage(name)
        }
        scanners += SubTypesScanner(true)
    }
    val reflections = Reflections(config)

    fun getContext(entityClass: KClass<*>): Set<KClass<out Any>> {
        val subClasses = reflections.getSubTypesOf(entityClass.java).map { it.kotlin }
        return setOf(entityClass, *subClasses.toTypedArray())
    }
}

internal sealed class ParsingResult {
    data class ValueExtracted(val parameter: KParameter, val value: Any?) : ParsingResult()
    object NoValue : ParsingResult()
}

/**
 * Build the an Entity using the given data list. Rules are:
 *
 * - the first element of the list is the name of the root entity
 * TODO complete doc
 *
 */
// TODO make sure the cast is actually really really safe
@Suppress("UNCHECKED_CAST")
fun <T : Any> buildEntity(dataList: List<String>,
                          entityClass: KClass<T>,
                          getContext: (entityClass: KClass<*>) -> Set<KClass<*>>,
                          isEntityNameMatch: (candidateClass: KClass<*>, entityName: String?) -> Boolean): T? {

    val entityName = if (dataList.isNotEmpty()) dataList[0] else null

    val candidates = getContext(entityClass)

    return candidates.filter { candidateClass ->
        entityClass.isSuperclassOf(candidateClass) && isEntityNameMatch(candidateClass, entityName)
    }.flatMap { electedClass ->
        val primary = electedClass.primaryConstructor
        val others = electedClass.constructors.filter { it !== primary }
        listOf(primary, *others.toTypedArray()).filter { constructor ->
            constructor?.visibility == PUBLIC
        }.map { constructor ->
            constructor!! // safe because the visibility test will fail if the method is null
        }
    }.map { constructor ->
        try {
            val values = constructor.parameters.map { param ->

                // dataList[0] is the entity name so add one to the index
                val currentURLPart = if (dataList.size > param.index + 1) dataList[param.index + 1] else null

                @Suppress("IMPLICIT_CAST_TO_ANY")
                val value = when (param.type.javaType) {
                    Int::class.java -> currentURLPart?.toInt()
                    Long::class.java -> currentURLPart?.toLong()
                    String::class.java -> currentURLPart
                // TODO maybe add more primitive type? Which one?
                    else -> {
                        // TODO I'm not happy with this but it's the only solution I've found
                        val paramClass = param.type.classifier as? KClass<T>
                        // drop every parts which has been consumed by the entity name or previous parameter
                        if (paramClass != null)
                            buildEntity(dataList.drop(param.index + 1), paramClass,
                                    getContext, isEntityNameMatch)
                        else null
                    }
                }
                if (value == null) {
                    // If the value is null, it's because we cannot read it from the URL
                    when {
                    // If the param have a default value, use it
                    // TODO we may want to introduce a choice here to decide if we prefer to use the default value or set to null
                        param.isOptional -> NoValue
                    // Else, if it's nullable then set to null
                        param.type.isMarkedNullable -> ValueExtracted(param, null)
                    // Else, there nothing we can decide here, throw to someone else
                        else -> throw IllegalArgumentException("Unable to build the entity, null value for $param")
                    }
                } else {
                    ValueExtracted(param, value)
                }
            }.filter { result ->
                // to use the default value, simply omit it
                result is ValueExtracted
            }.map { result ->
                val (parameter, value) = result as ValueExtracted
                parameter to value // convert into a pair for the mapOf
            }

            val args = mapOf(*values.toTypedArray())
            constructor.callBy(args)

        } catch(e: Exception) {
            // TODO better exception handling
            e.printStackTrace()
            null
        }
    }.filter { it != null }.firstOrNull() as T?
}
package io.kweb.routing

import io.kweb.pkg
import io.kweb.routing.ParsingResult.NoValue
import io.kweb.routing.ParsingResult.ValueExtracted
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaType

/**
 * Uses reflection to convert between a Kotlin data object and a
 * [URL obsPath](https://url.spec.whatwg.org/#concept-url-obsPath), and back again.
 *
 * @author [Desprez Jean-Marc](https://github.com/jmdesprez, modifications)
 * @author [Ian Clarke](https://github.com/sanity)
 **/
class UrlPathTranslator(val contextProvider: ((KClass<*>) -> Set<KClass<out Any>>)?) {

    /**
     * @param pathObjectPackage The name of the package in which the obsPath data class and any
     *                          related classes are defined.
     */
    constructor(vararg pathObjectPackage: String) : this(if (pathObjectPackage.isEmpty()) null else ClasspathScanner(*pathObjectPackage)::getContext)

    /**
     * Build the an Entity using the given data list.  For internal use only, but must
     * be public because [UrlPathTranslator.parse] calls it and is public.
     *
     * @suppress
     */
    // TODO make sure the cast is actually really really safe
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> buildEntity(dataList: List<String>,
                                            entityClass: KClass<T>,
                                            isEntityNameMatch: (candidateClass: KClass<*>, entityName: String?) -> Boolean): T? {

        val entityName = if (dataList.isNotEmpty()) dataList[0] else null

        val candidates = contextProvider.let {
            if (it != null) {
                it(entityClass)
            } else {
                ClasspathScanner(entityClass.pkg).getContext(entityClass)
            }
        }

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
                val values = constructor.parameters.map { param ->

                    // dataList[0] is the entity name so add one to the index
                    val currentURLPart = if (dataList.size > param.index + 1) dataList[param.index + 1] else null

                    @Suppress("IMPLICIT_CAST_TO_ANY")
                    val value = when (param.type.javaType) {
                        Int::class.java -> currentURLPart?.toInt()
                        Long::class.java -> currentURLPart?.toLong()
                        Boolean::class.java -> when(currentURLPart?.toLowerCase()) {
                            in setOf("true", "yes") -> true
                            in setOf("false", "no") -> false
                            else -> null
                        }
                        String::class.java -> currentURLPart
                    // TODO maybe add more primitive type? Which one?
                        else -> {
                            // TODO I'm not happy with this but it's the only solution I've found
                            val paramClass = param.type.classifier as? KClass<T>
                            // drop every parts which has been consumed by the entity name or previous parameter
                            if (paramClass != null)
                                buildEntity(dataList.drop(param.index + 1), paramClass, isEntityNameMatch)
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
                            else -> throw UrlParseException("Unable to parse url part $dataList because no value can be found for $param")
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

        }.firstOrNull() as T?
    }

    /**
     * Convert an object to a obsPath
     *
     * @sample toPath_sample
     */
    fun toPath(obj: Any): String = "/" + toPathList(obj).joinToString(separator = "/")

    internal fun toPath_sample() {
        data class Users(val userId : Int)
        val urlPathTranslator = UrlPathTranslator("io.kweb.routing")
        val pathAsString = urlPathTranslator.toPath(Users(152))
        assert(pathAsString == "/users/152")
    }

    private fun toPathList(obj: Any): List<String> {
        val entityPathElement = obj::class.simpleName!!.toLowerCase().let {
            if (it == "root") {
                emptyList()
            } else {
                listOf(it)
            }
        }
        val declaredMemberProperties = obj::class.declaredMemberProperties
        val params = declaredMemberProperties.map { property ->
            val parameterValue = property.call(obj)
            val parameterValueAsPathElements: List<String> = when (parameterValue) {
                is Int -> listOf<String>(parameterValue.toString())
                is Long -> listOf<String>(parameterValue.toString())
                is Boolean -> listOf<String>(parameterValue.toString())
                is String -> listOf<String>(parameterValue.toString())
                else -> if (parameterValue != null) toPathList(parameterValue) else emptyList<String>()
            }
            parameterValueAsPathElements
        }.flatMap { it }
        return entityPathElement + params
    }
}

/**
 * Convert a obsPath to an object.
 *
 * @sample parse_sample
 */

inline fun <reified T : Any> UrlPathTranslator.parse(url: String): T {
    val parts = url.trim('/').split('/').filter { it.isNotEmpty() }

    return buildEntity(parts, T::class) { kClass, entityName ->
        val realEntityName = entityName ?: "root"
        val name = kClass.simpleName?.toLowerCase()
        name == realEntityName
    }.let { entity ->
        entity ?: throw UrlParseException("Unable to parse URL obsPath `$url`")
    }
}

internal fun parse_sample() {
    data class Items(val itemId : Int)
    data class Users(val userId : Int, val item: Items)
    val urlPathTranslator = UrlPathTranslator("io.kweb.routing")
    val pathAsObject = urlPathTranslator.parse<Users>("/users/152/items/12")
    assert(pathAsObject == Users(userId = 152, item = Items(itemId = 12)))
}

class UrlParseException(reason: String) : Exception(reason)


// TODO This is not optimal because a child of a sealed class no longer need to be nested
private fun <T : Any> KClass<T>.meAndNested() = setOf(this, *nestedClasses.toTypedArray())

class ClasspathScanner(vararg val packages: String) {
    val config = ConfigurationBuilder().apply {
        packages.map { name ->
            urls += ClasspathHelper.forPackage(name)
        }
        scanners += SubTypesScanner(true)
    }
    val reflections = Reflections(config)

    fun getContext(entityClass: KClass<*>): Set<KClass<out Any>> {
        // TODO: I think we need to cache this, it is very slow - ian
        val subClasses = reflections.getSubTypesOf(entityClass.java).map { it.kotlin }
        return setOf(entityClass, *subClasses.toTypedArray())
    }
}

internal sealed class ParsingResult {
    data class ValueExtracted(val parameter: KParameter, val value: Any?) : ParsingResult()
    object NoValue : ParsingResult()
}

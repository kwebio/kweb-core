package io.kweb.routing

import io.ktor.routing.*
import io.ktor.routing.RoutingPathSegmentKind.*
import io.kweb.*
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new
import io.kweb.plugins.viewport.ViewportPlugin
import io.kweb.state.*
import io.kweb.state.persistent.render
import io.mola.galimatias.URL
import mu.KotlinLogging
import kotlin.collections.set

/**
 * @sample testSampleForRouting
 *
 * Created by @@jmdesprez, some modifications by @sanity
 */

// TODO: Handle back button https://www.webdesignerdepot.com/2013/03/how-to-manage-the-back-button-with-javascript/

private val logger = KotlinLogging.logger {}

fun main() {
    testSampleForRouting()
}

fun WebBrowser.pushState(path: String) {
    val url = URL.parse(path)
    execute("""
        history.pushState({}, "", location.origin+"${url.path()}");
        """.trimIndent())
}

fun <T : Any> WebBrowser.url(mapper: (String) -> T) = url.map(mapper)

fun <T : Any> WebBrowser.url(func: ReversableFunction<String, T>) = url.map(func)

val simpleUrlParser = object : ReversableFunction<String, URL>("simpleUrlParser") {
    override fun invoke(from: String): URL = URL.parse(from)

    override fun reverse(original: String, change: URL) = change.toString()

}


fun ElementCreator<*>.route(routeReceiver: RouteReceiver.() -> Unit) {
    val url = this.browser.url(simpleUrlParser)
    val rr = RouteReceiver(this, url)
    routeReceiver(rr)
    val pathKvar = url.path
    val matchingTemplate : KVal<PathTemplate?> = pathKvar.map { path ->
        rr.templatesByLength[path.size]?.keys?.firstOrNull { tpl ->
            tpl.withIndex().all {
                val tf = it.value.kind != Constant || path[it.index] == it.value.value
                tf
            }
        }
    }

    render(matchingTemplate) { template ->
        if (template != null) {
            val parameters = HashMap<String, KVal<String>>()
            for ((pos, part) in template.withIndex()) {
                if (part.kind == Parameter) {
                    parameters[part.value.substring(1, part.value.length - 1)] = pathKvar[pos]
                }
            }

            val pathRenderer = rr.templatesByLength[template.size]?.get(template)

            if(pathRenderer != null) {
                pathRenderer(this, parameters)
            } else {
                throw RuntimeException("Unable to find pathRenderer for template $template")
            }
        }
    }
}

typealias PathTemplate = List<RoutingPathSegment>
typealias PathReceiver = ElementCreator<*>.(Map<String, KVal<String>>) -> Unit

class RouteReceiver internal constructor(val parentElementCreator: ElementCreator<*>, val url: KVar<URL>) {
    internal val templatesByLength = HashMap<Int, MutableMap<PathTemplate, PathReceiver>>()

    fun path(template : String, pathReceiver : PathReceiver) {
        val routingPath = RoutingPath.parse(template).parts
        templatesByLength.computeIfAbsent(routingPath.size) {HashMap()}[routingPath]= pathReceiver
    }
}

operator fun <T : Any> KVal<List<T>>.get(pos: Int): KVal<T> {
    return this.map { it[pos] }
}

operator fun <T : Any> KVar<List<T>>.get(pos: Int): KVar<T> {
    return this.map(object : ReversableFunction<List<T>, T>("get($pos)") {
        override fun invoke(from: List<T>): T {
            return try {
                from[pos]
            } catch (e: IndexOutOfBoundsException) {
                throw kotlin.IndexOutOfBoundsException("Index $pos out of bounds in list $from")
            }
        }

        override fun reverse(original: List<T>, change: T) = original
                .subList(0, pos)
                .plus(change)
                .plus(original.subList(pos + 1, original.size))
    })
}

fun <T : Any> KVar<List<T>>.subList(fromIx: Int, toIx: Int): KVar<List<T>> = this.map(object : ReversableFunction<List<T>, List<T>>("subList($fromIx, $toIx)") {
    override fun invoke(from: List<T>): List<T> = from.subList(fromIx, toIx)

    override fun reverse(original: List<T>, change: List<T>): List<T> {
        return original.subList(0, fromIx) + change + original.subList(toIx, original.size)
    }
})

fun <T : Any> KVal<List<T>>.subList(fromIx: Int, toIx: Int): KVal<List<T>> = this.map { it.subList(fromIx, toIx) }

enum class Scheme {
    http, https
}

private val prx = "/".toRegex()

val KVar<URL>.path
    get() = this.map(object : ReversableFunction<URL, List<String>>("URL.path") {

        override fun invoke(from: URL): List<String> = from.pathSegments()

        override fun reverse(original: URL, change: List<String>): URL =
                original.withPath(change.joinToString(separator = "/"))

    })

private fun testSampleForRouting() {
    Kweb(port = 1234, plugins = listOf(ViewportPlugin())) {
        doc.body.new {
            route {
                path("/mouse/{id}") { params ->
                    val id = params.getValue("id")
                    h1().text(id.map { "Mouse #$it" })
                }
            }
        }
    }
}
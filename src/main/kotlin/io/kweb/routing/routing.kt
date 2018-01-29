package io.kweb.routing

import io.kweb.*
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new
import io.kweb.state.*
import io.kweb.state.persistent.render
import io.mola.galimatias.URL



/**
 * @sample test_sample_for_routing
 *
 * Created by @@jmdesprez, some modifications by @sanity
 */

// TODO: Handle window.onpopstate so that back buttons will work in a sensible way

fun main(args: Array<String>) {
    val url = URL.parse("http://a.b.c/hello?dog=cat&mouse=pig")
    println(url)
}

fun WebBrowser.pushState(path: String) {
    execute("""history.pushState({}, "$path", "$path");""")
}

fun WebBrowser.route(receiver: (url: Bindable<String>) -> Unit) {
    val url = Bindable(httpRequestInfo.requestedUrl)

    url.addListener { _, new ->
        pushState(new)
    }

    receiver(url)
}

fun <T : Any> WebBrowser.route(mapper: (String) -> T, receiver: (url: ReadOnlyBindable<T>) -> Unit) = route { receiver(it.map(mapper)) }

fun <T : Any> WebBrowser.route(func: ReversableFunction<String, T>, receiver: (url: Bindable<T>) -> Unit) = route { receiver(it.map(func)) }

val withGalimatiasUrlParser = object : ReversableFunction<String, URL> {
    override fun map(from: String): URL = URL.parse(from)

    override fun unmap(original: String, change: URL) = change.toString()

}

operator fun <T : Any> ReadOnlyBindable<List<T>>.get(pos: Int): ReadOnlyBindable<T> {
    return this.map { it[pos] }
}

operator fun <T : Any> Bindable<List<T>>.get(pos: Int): Bindable<T> {
    return this.map(object : ReversableFunction<List<T>, T> {
        override fun map(from: List<T>): T = from[pos]

        override fun unmap(list: List<T>, item: T) = list.subList(0, pos).plus(item).plus(list.subList(pos + 1, list.size))
    })
}

enum class Scheme {
    http, https
}

private val prx = "/".toRegex()

fun Bindable<URL>.path() = this.map(object : ReversableFunction<URL, List<String>> {

    override fun map(from: URL): List<String> = from.pathSegments()

    override fun unmap(original: URL, change: List<String>) =
            original.withPath(change.joinToString(separator = "/"))

})


///////// Sample code

data class Route(val a : Int)

private fun test_sample_for_routing() {
    Kweb(port= 16189) {
        doc.body.new {
            route { url ->
                render(url) { url ->
                    h1().text(url)
                }
            }
        }
    }

}
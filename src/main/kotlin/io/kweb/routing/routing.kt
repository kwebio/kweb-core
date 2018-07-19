package io.kweb.routing

import io.kweb.*
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new
import io.kweb.state.*
import io.mola.galimatias.URL
import mu.KotlinLogging

/**
 * @sample testSampleForRouting
 *
 * Created by @@jmdesprez, some modifications by @sanity
 */

// TODO: Handle back button https://www.webdesignerdepot.com/2013/03/how-to-manage-the-back-button-with-javascript/

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val url = URL.parse("http://a.b.c/hello?dog=cat&mouse=pig")
    println(url)
}

fun WebBrowser.pushState(path: String) {
    val url = URL.parse(path)
    execute("""
        history.pushState({}, "", location.origin+"${url.path()}");
        """.trimIndent())
}

private

// TODO: Perhaps some caching so that we don't re-parse the same URL repeatedly
fun <T : Any> WebBrowser.url(mapper: (String) -> T) = url.map(mapper)

fun <T : Any> WebBrowser.url(func: ReversableFunction<String, T>) = url.map(func)

val simpleUrlParser = object : ReversableFunction<String, URL>("simpleUrlParser") {
    override fun invoke(from: String): URL = URL.parse(from)

    override fun reverse(original: String, change: URL) = change.toString()

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

///////// Sample code

data class Route(val a: Int)

private fun testSampleForRouting() {
    Kweb(port = 16189) {
        doc.body.new {
            h1().text(url)
        }
    }

}
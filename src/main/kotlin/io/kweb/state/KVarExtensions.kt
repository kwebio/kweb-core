package io.kweb.state

import io.mola.galimatias.URL

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
    get() = this.map(object : ReversableFunction<URL, String>("URL.path") {

        override fun invoke(from: URL): String = from.path()

        override fun reverse(original: URL, change: String): URL =
                original.withPath(change)

    })

val KVar<URL>.pathSegments
    get() = this.map(object : ReversableFunction<URL, List<String>>("URL.pathSegments") {

        override fun invoke(from: URL): List<String> {
            return from.pathSegments()
        }

        override fun reverse(original: URL, change: List<String>): URL {
            return original.withPath("/" + if (change.isEmpty()) "" else change.joinToString(separator = "/"))
        }

    })

val simpleUrlParser = object : ReversableFunction<String, URL>("simpleUrlParser") {
    override fun invoke(from: String): URL = URL.parse(from)

    override fun reverse(original: String, change: URL) = change.toString()

}

fun KVar<String>.toInt() = this.map(object : ReversableFunction<String, Int>(label = "KVar<String>.toInt()") {
    override fun invoke(from: String) = from.toInt()

    override fun reverse(original: String, change: Int): String {
        return change.toString()
    }
})
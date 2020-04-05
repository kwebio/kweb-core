package kweb.state

import io.mola.galimatias.URL
import kweb.pathQueryFragment

operator fun <T : Any> KVar<List<T>>.get(pos: Int): KVar<T> {
    return this.map(object : ReversibleFunction<List<T>, T>("get($pos)") {
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

operator fun <K : Any, V : Any> KVar<Map<K, V>>.get(k : K) : KVar<V?> {
    return this.map(object : ReversibleFunction<Map<K, V>, V?>("map[$k]") {
        override fun invoke(from: Map<K, V>): V? = from[k]

        override fun reverse(original: Map<K, V>, change: V?): Map<K, V> {
            return if (change != null) {
                original + (k to change)
            } else {
                original - k
            }
        }
    })
}

fun <T : Any> KVar<List<T>>.subList(fromIx: Int, toIx: Int): KVar<List<T>> = this.map(object : ReversibleFunction<List<T>, List<T>>("subList($fromIx, $toIx)") {
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
    get() = this.map(object : ReversibleFunction<URL, String>("URL.path") {

        override fun invoke(from: URL): String = from.path()

        override fun reverse(original: URL, change: String): URL =
                original.withPath(change)

    })

val KVar<URL>.query
    get() = this.map(object : ReversibleFunction<URL, String?>("URL.query") {

        override fun invoke(from: URL): String? = from.query()

        override fun reverse(original: URL, change: String?): URL =
                original.withQuery(change)

    })

val KVar<URL>.pathSegments
    get() = this.map(object : ReversibleFunction<URL, List<String>>("URL.pathSegments") {

        override fun invoke(from: URL): List<String> {
            return from.pathSegments()
        }

        override fun reverse(original: URL, change: List<String>): URL {
            return original.withPath("/" + if (change.isEmpty()) "" else change.joinToString(separator = "/"))
        }

    })

/**
 * Given the URI specification:
 *
 * `URI = scheme:[//authority]path[?query][#fragment]`
 *
 * The `pqf` refers to the `path[?query][#fragment]` and can be used to change the path, query, and/or fragment
 * of the URL in one shot.
 */
val KVar<URL>.pathQueryFragment get() = this.map(object : ReversibleFunction<URL, String>("URL.pathQueryFragment") {
    override fun invoke(from: URL): String {
        return from.pathQueryFragment
    }

    override fun reverse(original: URL, change: String): URL {
        return original.resolve(change)
    }
})

fun <A, B> Pair<KVar<A>, KVar<B>>.combine() : KVar<Pair<A, B>> {
    val newKVar = KVar(this.first.value to this.second.value)
    this.first.addListener { o, n -> newKVar.value = n to this.second.value }
    this.second.addListener {o, n -> newKVar.value = this.first.value to n }

    newKVar.addListener { o, n ->
        this.first.value = n.first
        this.second.value = n.second
    }
    return newKVar
}

val simpleUrlParser = object : ReversibleFunction<String, URL>("simpleUrlParser") {
    override fun invoke(from: String): URL = URL.parse(from)

    override fun reverse(original: String, change: URL) = change.toString()

}

infix operator fun KVar<String>.plus(s : String) = this.map { it + s }
infix operator fun String.plus(sKV : KVar<String>) = sKV.map { this + it }

fun KVar<String>.toInt() = this.map(object : ReversibleFunction<String, Int>(label = "KVar<String>.toInt()") {
    override fun invoke(from: String) = from.toInt()

    override fun reverse(original: String, change: Int): String {
        return change.toString()
    }
})

package kweb.routing

import kweb.state.ReversibleFunction

internal object UrlToPathSegmentsRF : ReversibleFunction<String, List<String>>(label = "UrlToPathSegmentsRF") {
    override fun invoke(from: String): List<String> {
        return from.substringBefore('?').split('/').drop(1)
    }

    override fun reverse(original: String, change: List<String>): String {
        val queryFragment = original.substringAfter('?')
        return '/' + change.joinToString(separator = "/") + '?' + queryFragment
    }
}
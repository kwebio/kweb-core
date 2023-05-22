package kweb.routing

import kweb.state.ReversibleFunction

internal object UrlToPathSegmentsRF : ReversibleFunction<String, List<String>>(label = "UrlToPathSegmentsRF") {
    override fun invoke(from: String): List<String> {
        // Extract the path segments before the query parameters and URL fragment.
        return from.substringBefore('?').substringBefore('#').split('/').drop(1)
    }

    override fun reverse(original: String, change: List<String>): String {
        // Extract the query parameters and URL fragment from the original URL.
        val queryFragment = original.substringAfter('?', missingDelimiterValue = "").substringBefore('#')
        val fragment = original.substringAfter('#', missingDelimiterValue = "")

        // Reconstruct the URL with the path segments, query parameters, and URL fragment.
        return '/' + change.joinToString(separator = "/") +
                (if (queryFragment != "") "?$queryFragment" else "") +
                (if (fragment != "") "#$fragment" else "")
    }
}

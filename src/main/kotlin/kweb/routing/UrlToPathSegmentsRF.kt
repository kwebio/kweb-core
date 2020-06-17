package kweb.routing

import kweb.state.ReversibleFunction

internal object UrlToPathSegmentsRF : ReversibleFunction<String, List<String>>(label = "UrlToPathSegmentsRF") {
    override fun invoke(from: String): List<String> {
        return from.substringBefore('?').split('/').drop(1)
    }

    override fun reverse(originalUrl: String, newPath: List<String>): String {
        val queryFragment = originalUrl.substringAfter('?', missingDelimiterValue = "")
        return '/' + newPath.joinToString(separator = "/") + (if (queryFragment != "") "?$queryFragment" else "")
    }
}
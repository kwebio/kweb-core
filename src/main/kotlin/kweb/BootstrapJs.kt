package kweb

import kweb.util.Template

object BootstrapJs {
    private const val clientIdToken = "--CLIENT-ID-PLACEHOLDER--"
    private const val buildPageToken = "<!-- BUILD PAGE PAYLOAD PLACEHOLDER -->"
    private const val functionCacheToken = "// FUNCTION CACHE PLACEHOLDER //"
    //private const val functionCacheToken = "<!-- FUNCTION CACHE PLACEHOLDER -->"
    private const val offlineToastMessage = "-- TOAST MESSAGE PLACEHOLDER --"

    private val template: Template by lazy {
        Kweb::class.java.getResourceAsStream("kweb_bootstrap.js").use { resourceStream ->
            checkNotNull(resourceStream) { "Could not load kweb_bootstrap.js" }
            val jsAsString = resourceStream.reader().readText()
            // By storing the Template we only need to locate the tokens once
            Template(jsAsString, clientIdToken, buildPageToken, functionCacheToken, offlineToastMessage)
        }
    }

    /**
     * Efficiently inject required data into kweb_bootstrap.js
     */
    fun hydrate(clientId: String, pageBuildInstructions: String, functionCache: String, offlineToastMessageStr: String): String {
        return template.apply(clientId, pageBuildInstructions, functionCache, offlineToastMessageStr)
    }
}

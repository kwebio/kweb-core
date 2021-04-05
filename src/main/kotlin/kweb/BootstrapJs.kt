package kweb

import kweb.util.Template
import org.apache.commons.io.IOUtils

object BootstrapJs {
    private const val clientIdToken = "--CLIENT-ID-PLACEHOLDER--"
    private const val buildPageToken = "<!-- BUILD PAGE PAYLOAD PLACEHOLDER -->"
    private const val functionCacheToken = "<!-- FUNCTION CACHE PLACEHOLDER -->"

    private val template: Template by lazy {
        Kweb::class.java.getResourceAsStream("kweb_bootstrap.js").use { resourceStream ->
            val jsAsString = IOUtils.toString(resourceStream, "UTF-8")
            // By storing the Template we only need to locate the tokens once
            Template(jsAsString, clientIdToken, buildPageToken, functionCacheToken)
        }
    }

    /**
     * Efficiently inject required data into kweb_bootstrap.js
     */
    fun hydrate(clientId: String, pageBuildInstructions: String, functionCache: String): String {
        return template.apply(clientId, pageBuildInstructions, functionCache)
    }
}

fun tmp() {
    Kweb(port = 1234) {
        doc.body.new {
            ul().new {
                li().text("One")
                li().text("Two")
                li().text("Three")
            }
        }
    }
}

fun tmp2() {
    Kweb(port = 1234) {
        doc.body {
            ul {
                li().text("One")
                li().text("Two")
                li().text("Three")
            }
        }
    }
}

package kweb

import kweb.common.Template
import org.apache.commons.io.IOUtils

object BootstrapJs {
    private const val clientIdToken = "--CLIENT-ID-PLACEHOLDER--"
    private const val buildPageToken = "<!-- BUILD PAGE PAYLOAD PLACEHOLDER -->"

    private val template: Template by lazy {
        Kweb::class.java.getResourceAsStream("kweb_bootstrap.js").use { resourceStream ->
            val jsAsString = IOUtils.toString(resourceStream, "UTF-8")
            // By storing the Template we only need to locate the tokens once
            Template(jsAsString, clientIdToken, buildPageToken)
        }
    }

    /**
     * Efficiently inject required data into kweb_bootstrap.js
     */
    fun hydrate(clientId: String, pageBuildInstructions: String): String {
        return template.apply(clientId, pageBuildInstructions)
    }
}
package io.kweb

import io.kweb.common.Template
import org.apache.commons.io.IOUtils

object BootstrapJs {
    private const val clientIdToken = "--CLIENT-ID-PLACEHOLDER--"
    private const val buildPageToken = "<!-- BUILD PAGE PAYLOAD PLACEHOLDER -->"

    private val template : Template by lazy {
        val resourceStream = Kweb::class.java.getResourceAsStream("kweb_bootstrap.js")
        val jsAsString = IOUtils.toString(resourceStream, Charsets.UTF_8)
                // This is required to prevent JSoup from mangling the script
            //    .replace("<", "&lt;")
        Template(jsAsString, clientIdToken, buildPageToken)
    }

    fun hydrate(clientId : String, pageBuildInstructions : String) : String {
        return template.apply(clientId, pageBuildInstructions)
    }
}
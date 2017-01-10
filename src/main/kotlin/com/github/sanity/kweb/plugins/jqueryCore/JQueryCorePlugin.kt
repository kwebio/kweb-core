package com.github.sanity.kweb.plugins.jqueryCore

import com.github.sanity.kweb.RootReceiver
import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.toJson

/**
 * Created by ian on 1/9/17.
 */
class JQueryCorePlugin : KWebPlugin {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        // Include the plugin, this is straight from the JQuery documentation
        startHead.appendln("""
        <script
                src="https://code.jquery.com/jquery-3.1.1.min.js"
        integrity="sha256-hVVnYaiADRTO2PzUGmuLJr8BLUSjGIZsDYGmIJLv2b8="
        crossorigin="anonymous"></script>""".trimIndent()
        )
    }
}

// A convenience value
val jqueryCore = JQueryCorePlugin()

// Support for $(...), since Kotlin doesn't allow methods called '$' (which is probably a good thing)
// I just use jquery()
fun RootReceiver.jquery(selector: String): JQueryReceiver {
    require(JQueryCorePlugin::class)
    return JQueryReceiver(this, "$(${selector.toJson()})")
}

// And here we can implement all of the useful JQuery functions
class JQueryReceiver(private val rootReceiver : RootReceiver, private val js : String) {
    fun remove() {
        rootReceiver.execute(js+".remove();")
    }
}
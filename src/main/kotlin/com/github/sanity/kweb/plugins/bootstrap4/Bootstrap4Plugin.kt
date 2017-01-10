package com.github.sanity.kweb.plugins.bootstrap4

import com.github.sanity.kweb.RootReceiver
import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.plugins.jqueryCore.JQueryCorePlugin
import com.github.sanity.kweb.toJson

/**
 * Created by ian on 1/10/17.
 */

class Bootstrap4Plugin : KWebPlugin {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        // From https://v4-alpha.getbootstrap.com/getting-started/download/#source-files
        startHead.appendln("""
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css" integrity="sha384-rwoIResjU2yc3z8GV/NPeZWAv56rSmLldC3R/AZzGRnGxQQKnKkoFVhFQhNUwEyJ" crossorigin="anonymous">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/js/bootstrap.min.js" integrity="sha384-vBWWzlZJ8ea9aCX4pEW3rVHjgjt7zpkNpZk+02D9phzyeVkE+jo0ieGizqPLForn" crossorigin="anonymous"></script>
        """.trimIndent()
        )
    }
}

// A convenience value
val bootstrap4 = JQueryCorePlugin()

// Support for $(...), since Kotlin doesn't allow methods called '$' (which is probably a good thing)
// I just use jquery()
fun RootReceiver.jquery(selector: String): JQueryReceiver {
    require(JQueryCorePlugin::class)
    return JQueryReceiver(this, "$(${selector.toJson()})")
}

// And here we can implement all of the useful JQuery functions
class JQueryReceiver(private val rootReceiver: RootReceiver, private val js: String) {
    fun remove() {
        rootReceiver.execute(js + ".remove();")
    }
}
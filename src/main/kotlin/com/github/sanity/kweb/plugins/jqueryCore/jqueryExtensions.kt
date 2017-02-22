package com.github.sanity.kweb.plugins.jqueryCore

import com.github.sanity.kweb.RootReceiver
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.KWebDSL
import com.github.sanity.kweb.toJson

/**
 * Created by ian on 2/22/17.
 */


// Support for $(...), since Kotlin doesn't allow methods called '$' (which is probably a good thing)
// I just use jquery()
fun Element.jquery(selector: String = "#${this.id}"): JQueryReceiver {
    require(JQueryCorePlugin::class)
    return JQueryReceiver(this.rootReceiver, "$(${selector.toJson()})")
}

// And here we can implement all of the useful JQuery functions
@KWebDSL
class JQueryReceiver(internal val rootReceiver : RootReceiver, internal val js : String) {
    val on : JQueryOnReceiver get() = JQueryOnReceiver(this)

    fun remove() {
        rootReceiver.execute(js+".remove();")
    }
}

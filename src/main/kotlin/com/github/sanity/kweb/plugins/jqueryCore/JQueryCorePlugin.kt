package com.github.sanity.kweb.plugins.jqueryCore

import com.github.sanity.kweb.dom.Element
import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.toJson

/**
 * Created by ian on 1/9/17.
 */
class JQueryCorePlugin : KWebPlugin {
    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        startHead.appendln("""
        <script
                src="https://code.jquery.com/jquery-3.1.1.min.js"
        integrity="sha256-hVVnYaiADRTO2PzUGmuLJr8BLUSjGIZsDYGmIJLv2b8="
        crossorigin="anonymous"></script>""".trimIndent()
        )
    }
}

val jqueryCore = JQueryCorePlugin()

fun Element.jquery(selector: String) = JQueryReceiver(this, "$(${selector.toJson()})")

class JQueryReceiver(private val parent : Element, private val js : String) {
    fun remove() {
        parent.execute(js+".remove();")
    }
}
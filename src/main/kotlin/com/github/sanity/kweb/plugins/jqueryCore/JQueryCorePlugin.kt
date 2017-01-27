package com.github.sanity.kweb.plugins.jqueryCore

import com.github.sanity.kweb.RootReceiver
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.KWebDSL
import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.random
import com.github.sanity.kweb.toJson

/**
 * Created by ian on 1/9/17.
 */
class JQueryCorePlugin : KWebPlugin() {
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
fun Element.jquery(selector: String): JQueryReceiver {
    require(JQueryCorePlugin::class)
    return JQueryReceiver(this.rootReceiver, "$(${selector.toJson()})")
}

// And here we can implement all of the useful JQuery functions
@KWebDSL
class JQueryReceiver(private val rootReceiver : RootReceiver, private val js : String) {
    // TODO: Support querying the event eg. to determine what key was pressed
    fun on(event: String, rh: RootReceiver.() -> Unit) : JQueryReceiver {
        val callbackId = Math.abs(random.nextInt())
        rootReceiver.executeWithCallback("$js.on(${event.toJson()}, function() {callbackWs($callbackId, false);})", callbackId) {
            rh.invoke(rootReceiver)
        }
        return this
    }

    // From http://www.w3schools.com/jquery/jquery_ref_events.asp, incomplete
    fun blur(rh: RootReceiver.() -> Unit) = on("blur", rh)
    fun change(rh: RootReceiver.() -> Unit) = on("change", rh)
    fun click(rh: RootReceiver.() -> Unit) = on("click", rh)
    fun dblclick(rh: RootReceiver.() -> Unit) = on("dblclick", rh)
    fun focus(rh: RootReceiver.() -> Unit) = on("focus", rh)
    fun focusin(rh: RootReceiver.() -> Unit) = on("focusin", rh)
    fun focusout(rh: RootReceiver.() -> Unit) = on("focusout", rh)
    fun hover(rh: RootReceiver.() -> Unit) = on("hover", rh)
    fun keydown(rh: RootReceiver.() -> Unit) = on("keydown", rh)
    fun mousedown(rh: RootReceiver.() -> Unit) = on("mousedown", rh)
    fun mouseenter(rh: RootReceiver.() -> Unit) = on("mouseenter", rh)
    fun mouseleave(rh: RootReceiver.() -> Unit) = on("mouseleave", rh)
    fun mousemove(rh: RootReceiver.() -> Unit) = on("mousemove", rh)
    fun mouseout(rh: RootReceiver.() -> Unit) = on("mouseout", rh)
    fun mouseup(rh: RootReceiver.() -> Unit) = on("mouseup", rh)

    fun remove() {
        rootReceiver.execute(js+".remove();")
    }
}
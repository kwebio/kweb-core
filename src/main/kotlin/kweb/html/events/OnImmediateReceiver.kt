package kweb.html.events

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kweb.Kweb
import kweb.div
import kweb.h1
import kweb.plugins.fomanticUI.fomantic
import kweb.util.KWebDSL

@KWebDSL
class OnImmediateReceiver<T: EventGenerator<T>>(internal val source: T) {
    fun event(eventName: String, callback: () -> Unit): T {
        val caughtJsFunctions = source.browser.kweb.catchOutbound {
            callback()
        }
        val immediateJs = mutableListOf<String>()
        for (jsFunction in caughtJsFunctions) {
            if (jsFunction.arguments.isNotEmpty()) {
                val argStrings = mutableListOf<String>()
                for (arg in jsFunction.arguments) {
                    val argument = arg.toString()
                    println("Argument is $argument")
                    argStrings.add(argument)


                }
                immediateJs.add("cachedFunctions[${jsFunction.jsId}](${argStrings.joinToString(",")})")
            } else {
                immediateJs.add("cachedFunctions[${jsFunction.jsId}]()")
            }

        }

        source.addImmediateEventCode(eventName, immediateJs.joinToString(separator = ""))
        return source
    }

    // Mouse events
    fun click(callback: () -> Unit) = event("click", callback)
    fun contextmenu(callback: () -> Unit) = event("contextmenu", callback)
    fun dblclick(callback: () -> Unit) = event("dblclick", callback)
    fun mousedown(callback: () -> Unit) = event("mousedown", callback)
    fun mouseenter(callback: () -> Unit) = event("mouseenter", callback)
    fun mouseleave(callback: () -> Unit) = event("mouseleave", callback)
    fun mousemove(callback: () -> Unit) = event("mousemove", callback)
    fun mouseover(callback: () -> Unit) = event("mouseover", callback)
    fun mouseout(callback: () -> Unit) = event("mouseout", callback)
    fun mouseup(callback: () -> Unit) = event("mouseup", callback)

    // Keyboard events
    fun keydown(callback: () -> Unit) = event("keydown", callback)
    fun keypress(callback: () -> Unit) = event("keypress", callback)
    fun keyup(callback: () -> Unit) = event("keyup", callback)

    // Focus Events
    // https://www.w3schools.com/jsref/obj_focusevent.asp
    fun blur(callback: () -> Unit) = event("blur", callback)
    fun focus(callback: () -> Unit) = event("focus", callback)
    fun focusin(callback: () -> Unit) = event("focusin", callback)
    fun focusout(callback: () -> Unit) = event("focusout", callback)

    // Frame / Object Events
    fun abort(callback: () -> Unit) = event("abort", callback)
    fun beforeunload(callback: () -> Unit) = event("beforeunload", callback)
    fun error(callback: () -> Unit) = event("error", callback)
    fun hashchange(callback: () -> Unit) = event("hashchange", callback)
    fun load(callback: () -> Unit) = event("load", callback)
    fun pageshow(callback: () -> Unit) = event("blur", callback)
    fun pagehide(callback: () -> Unit) = event("pagehide", callback)
    fun resize(callback: () -> Unit) = event("resize", callback)
    fun scroll(callback: () -> Unit) = event("scroll", callback)
    fun unload(callback: () -> Unit) = event("unload", callback)

    // Form Events
    fun change(callback: () -> Unit) = event("change", callback)
    fun input(callback: () -> Unit) = event("input", callback)
    fun invalid(callback: () -> Unit) = event("invalid", callback)
    fun reset(callback: () -> Unit) = event("reset", callback)
    fun search(callback: () -> Unit) = event("search", callback)
    fun select(callback: () -> Unit) = event("select", callback)
    fun submit(callback: () -> Unit) = event("submit", callback)

    // Drag Events
    fun drag(callback: () -> Unit) = event("drag", callback)
    fun dragend(callback: () -> Unit) = event("dragend", callback)
    fun dragenter(callback: () -> Unit) = event("dragenter", callback)
    fun dragleave(callback: () -> Unit) = event("dragleave", callback)
    fun dragover(callback: () -> Unit) = event("dragover", callback)
    fun dragstart(callback: () -> Unit) = event("dragstart", callback)
    fun drop(callback: () -> Unit) = event("drop", callback)

    // Clipboard Events
    fun copy(callback: () -> Unit) = event("copy", callback)
    fun cut(callback: () -> Unit) = event("cut", callback)
    fun paste(callback: () -> Unit) = event("paste", callback)

    // Print Events
    fun afterprint(callback: () -> Unit) = event("afterprint", callback)
    fun beforeprint(callback: () -> Unit) = event("beforeprint", callback)

    // Selection Events
    fun selectstart(callback: () -> Unit) = event("selectstart", callback = callback)
    fun selectionchange(callback: () -> Unit) = event("selectionchange",  callback = callback)

    // Media events
    /*
    fun abort(callback: () -> Unit) = event("abort", callback)
    fun canplay(callback: () -> Unit) = event("canplay", callback)
    fun canplaythrough(callback: () -> Unit) = event("canplaythrough", callback)
    fun durationchange(callback: () -> Unit) = event("durationchange", callback)
    fun emptied(callback: () -> Unit) = event("emptied", callback)
    fun ended(callback: () -> Unit) = event("ended", callback)
    fun message(callback: () -> Unit) = event("message", callback)
    fun loadeddata(callback: () -> Unit) = event("loadeddata", callback)
    fun loadedmetadata(callback: () -> Unit) = event("loadedmetadata", callback)
    fun loadstart(callback: () -> Unit) = event("loadstart", callback)
    fun pause(callback: () -> Unit) = event("pause", callback)
    fun play(callback: () -> Unit) = event("play", callback)
    fun playing(callback: () -> Unit) = event("playing", callback)
    fun progress(callback: () -> Unit) = event("progress", callback)
    fun ratechange(callback: () -> Unit) = event("ratechange", callback)
    fun seeked(callback: () -> Unit) = event("seeked", callback)
    fun seeking(callback: () -> Unit) = event("seeking", callback)
    fun stalled(callback: () -> Unit) = event("stalled", callback)
    fun suspend(callback: () -> Unit) = event("suspend", callback)
    fun timeupdate(callback: () -> Unit) = event("timeupdate", callback)
    fun volumechange(callback: () -> Unit) = event("volumechange", callback)
    fun waiting(callback: () -> Unit) = event("waiting", callback)
    fun animationend(callback: () -> Unit) = event("animationend", callback)
    fun animationiteration(callback: () -> Unit) = event("animationiteration", callback)
    fun animationstart(callback: () -> Unit) = event("animationstart", callback)
    fun transitionend(callback: () -> Unit) = event("transitionend", callback)
     */
}

fun main() {
    val server: Kweb = Kweb(port= 7660) {
        doc.body {
            div(fomantic.content) {
                val label = h1(fomantic.ui)
                label.text("Click Me")
                label.onImmediate.click {
                    label.text("Clicked!")
                }
            }
        }
    }
}
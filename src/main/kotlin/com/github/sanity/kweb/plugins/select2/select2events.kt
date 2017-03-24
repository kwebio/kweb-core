package com.github.sanity.kweb.plugins.select2

import com.github.sanity.kweb.dom.element.creation.SelectCreator
import com.github.sanity.kweb.plugins.jqueryCore.JQueryOnReceiver
import com.github.sanity.kweb.plugins.jqueryCore.jquery

data class Select2Event(val params : Params) {
    // TODO: Assumes a single selection, may be making other assumptions
    // TODO: regarding event object passed in by select2 (see
    // TODO: https://select2.github.io/examples.html#events)
    val id get() = params.data.id
    val text get() = params.data.text
    val selected get() = params.data.selected
}

data class Params(val data : Data)

data class Data(val id : String, val text : String, val selected : Boolean)

class Select2OnReceiver(val wrapped: SelectCreator) : JQueryOnReceiver(wrapped.jquery()) {
    fun close(callback: (Select2Event) -> Unit) = event("select2:close", Select2Event::class, callback = callback)
    fun open(callback: (String) -> Unit) = event("select2:open", callback = callback)
    fun select(callback: (Select2Event) -> Unit) = event("select2:select", Select2Event::class, callback = callback)
    fun unselect(callback: (String) -> Unit) = event("select2:unselect", callback = callback)

}
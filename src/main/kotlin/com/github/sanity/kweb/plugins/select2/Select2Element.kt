package com.github.sanity.kweb.plugins.select2

import com.github.sanity.kweb.dom.element.creation.SelectCreator
import com.github.sanity.kweb.plugins.jqueryCore.jquery
import com.github.sanity.kweb.toJson

class Select2Element(val wrapped : SelectCreator) {
    val on : Select2OnReceiver get() = Select2OnReceiver(wrapped)

    fun open() {
        wrapped.jquery().execute("""select2("open")""")
    }

    fun close() {
        wrapped.jquery().execute("""select2("close")""")
    }

    fun init() {
        wrapped.jquery().execute("""select2("init")""")
    }

    fun destroy() {
        wrapped.jquery().execute("""select2("destroy")""")
    }

    fun change(id : String) {
        wrapped.jquery().execute("""val(${id.toJson()}).trigger("change")""")
    }

    fun multiChange(vararg ids : String) {
        wrapped.jquery().execute("""val(${ids.toJson()}).trigger("change")""")
    }
}
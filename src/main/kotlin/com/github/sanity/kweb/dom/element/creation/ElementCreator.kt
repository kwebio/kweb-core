package com.github.sanity.kweb.dom.element.creation

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.random
import com.github.sanity.kweb.toJson
import mu.KLogging
import kotlin.reflect.KClass

/**
 * Created by ian on 1/13/17.
 */


// these should return Element, not ElementCreators.  If they want to create more
// within a dsl let them type element.create().ul()...


open class ElementCreator<out PARENT_TYPE : Element>(val parent: PARENT_TYPE, val position : Int? = null) {

    companion object: KLogging()

    var elementCreationCount = 0

    fun element(tag: String, attributes: Map<String, Any> = attr): Element {
        elementCreationCount++
        if (position != null && elementCreationCount == 2) {
            logger.warn {
                """
                It's unwise to element multiple elements using the same ElementCreator when position is specified,
                because each element will be added at the same position among its siblings, which will result in them
                being inserted in reverse-order.
                """.trimIndent().trim() }
        }

        val id: String = (attributes["id"] ?: Math.abs(random.nextInt())).toString()
        val javaScript = StringBuilder()
        with(javaScript) {
            appendln("{")
            appendln("var newEl = document.element(\"$tag\");")
            if (!attributes.containsKey("id")) {
                appendln("newEl.setAttribute(\"id\", \"$id\");")
            }
            for ((name, value) in attributes) {
                appendln("newEl.setAttribute(\"$name\", ${value.toJson()});")
            }
            if (position == null) {
                appendln("${parent.jsExpression}.appendChild(newEl);")
            } else {
                appendln("${parent.jsExpression}.insertBefore(newEl, ${parent.jsExpression}.childNodes[$position]);")
            }
            appendln("}")
        }
        parent.execute(javaScript.toString())
        return Element(parent.rootReceiver, tag = tag, jsExpression = "document.getElementById(\"$id\")", id = id)
    }

    fun require(vararg plugins: KClass<out KWebPlugin>) = parent.rootReceiver.require(*plugins)

}
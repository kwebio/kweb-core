package com.github.sanity.kweb.plugins.foundation.grid

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.creation.DivCreator
import com.github.sanity.kweb.dom.element.creation.ElementCreator
import com.github.sanity.kweb.dom.element.modification.addClasses

/**
 * Created by ian on 3/24/17.
 */

val ElementCreator.foundation : FoundationCreator get() {
    require(com.github.sanity.kweb.plugins.foundation.foundation::class)
    return FoundationCreator(this)
}

class FoundationCreator(parent : ElementCreator) : ElementCreator(parent.element) {
    fun row(expanded : Boolean? = null) = div().parent.addClasses("expanded", onlyIf = expanded ?: false)
}

class RowCreator(parent : DivCreator) : DivCreator(parent.element) {
    fun column(small : Int? = null, medium : Int? = null, large : Int? = null) : DivCreator {
        assert(small != null || medium != null || large != null)
        val classes = ArrayList<String>()
        if (small != null)  classes.add("small-$small")
        if (medium != null)  classes.add("medium-$medium")
        if (large != null)  classes.add("large-$large")

        return div(attributes = attr.classes("column").classes(classes))
    }
}
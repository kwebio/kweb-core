package com.github.sanity.kweb.plugins.materialdesignlite.table

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.*
import com.github.sanity.kweb.dom.element.modification.setText
import com.github.sanity.kweb.plugins.materialdesignlite.MDLElement
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Created by ian on 1/23/17.
 */

fun MDLElement.table(selectable: Boolean = false, attributes: Map<String, Any> = attr): MDLTableElement
        = MDLTableElement(table(attr
        .classes("mdl-data-table", "mdl-js-data-table")
        .classes("mdl-data-table--selectable", onlyIf = selectable)
))

class MDLTableElement internal constructor(wrapped: Element) : TableElement(wrapped) {
    var theadUsed = false
    var tbodyUsed = false

    fun fromDataObjectList(objects: List<Any>, propertyOrder: Collection<KProperty1<*, *>>? = null) {
        kotlin.require(objects.isNotEmpty(), { "Object list must contain at least one object" })
        val firstObjectClass = objects.first()::class
        kotlin.require(objects.all { it::class == firstObjectClass }, { "All objects must be of the same type" })
        val properties = propertyOrder ?: firstObjectClass.memberProperties
        thead().apply {
            tr().apply {
                for (property in properties) {
                    val annotations = property.annotations
                    val annotation = annotations.find { it is MDLTableHeaderName }
                    val headerName = if (annotation == null) {
                        property.name
                    } else {
                        (annotation as MDLTableHeaderName).name
                    }
                    th().setText(headerName)
                }
            }
        }
        tbody().apply {
            for (obj in objects) {
                tr().apply {
                    for (property in properties) {
                        val value = property.call(obj)
                        td().setText(value.toString())
                    }
                }
            }
        }
    }

    override fun thead(attributes: Map<String, Any>): MDLTHeadElement {
        if (theadUsed) throw IllegalStateException("A table may only have one header, but thead() has been called already")
        theadUsed = true
        return MDLTHeadElement(super.thead(attributes))
    }

    override fun tbody(attributes: Map<String, Any>): MDLTBodyElement {
        tbodyUsed = true
        return MDLTBodyElement(super.tbody(attributes))
    }
}

class MDLTHeadElement(wrapped: Element) : TheadElement(wrapped) {
    override fun tr(attributes: Map<String, Any>): MDLTrHeadElement = MDLTrHeadElement(super.tr(attributes))
}

class MDLTBodyElement(wrapped: Element) : TbodyElement(wrapped) {
    override fun tr(attributes: Map<String, Any>): MDLTrBodyElement = MDLTrBodyElement(super.tr(attributes))
}

class MDLTrHeadElement(wrapped: Element) : TrHeadElement(wrapped) {
    fun th(
            sort: TableSortOrder = TableSortOrder.none,
            nonNumeric: Boolean = false,
            attributes: Map<String, Any>
    ): MDLThElement = MDLThElement(super.th(attributes
            .classes("mdl-data-table__header--sorted-ascending", onlyIf = sort == TableSortOrder.ascending)
            .classes("mdl-data-table__header--sorted-descending", onlyIf = sort == TableSortOrder.descending)
            .classes("mdl-data-table__cell--non-numeric", onlyIf = nonNumeric)
    ))

    override fun th(attributes: Map<String, Any>): MDLThElement {
        return MDLThElement(super.th(attributes))
    }
}

class MDLTrBodyElement(wrapped: Element) : TrBodyElement(wrapped) {
    fun td(
            sort: TableSortOrder = TableSortOrder.none,
            nonNumeric: Boolean = false,
            attributes: Map<String, Any>
    ): MDLThElement = MDLThElement(super.td(attributes
            .classes("mdl-data-table__header--sorted-ascending", onlyIf = sort == TableSortOrder.ascending)
            .classes("mdl-data-table__header--sorted-descending", onlyIf = sort == TableSortOrder.descending)
            .classes("mdl-data-table__cell--non-numeric", onlyIf = nonNumeric)
    ))

    override fun td(attributes: Map<String, Any>): MDLTdElement {
        return MDLTdElement(super.td(attributes))
    }
}

enum class TableSortOrder {
    ascending, descending, none
}

class MDLThElement(wrapped: Element) : ThElement(wrapped) {

}

class MDLTdElement(wrapped: Element) : TdElement(wrapped) {

}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class MDLTableHeaderName(val name: String)
package com.github.sanity.kweb.plugins.materialdesignlite.table

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.element.creation.*
import com.github.sanity.kweb.plugins.materialdesignlite.MDLCreator
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Created by ian on 1/23/17.
 */

fun MDLCreator.table(selectable: Boolean = false, attributes: Map<String, Any> = attr): MDLTableElement
        = MDLTableElement(element.create().table(attributes
        .classes("mdl-data-table", "mdl-js-data-table")
        .classes("mdl-data-table--selectable", onlyIf = selectable)
).element)

class MDLTableElement internal constructor(wrapped: Element) : TableCreator(wrapped) {
    var theadUsed = false
    var tbodyUsed = false

    fun <T : Any> fromDataObjectList(objects: List<T>, propertyOrder: Collection<KProperty1<*, *>>? = null, tdModifier: (T, MDLTdElement) -> Unit = { _, _ -> }) {
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
                    th().text(headerName)
                }
            }
        }
        tbody().apply {
            for (obj in objects) {
                tr().apply {
                    for (property in properties) {
                        val value = property.call(obj)
                        val tdElement = td()
                        tdElement.text(value.toString())
                        tdModifier(obj, tdElement)
                    }
                }
            }
        }
    }

    override fun thead(attributes: Map<String, Any>): MDLTHeadCreator {
        if (theadUsed) throw IllegalStateException("A table may only have one header, but thead() has been called already")
        theadUsed = true
        return MDLTHeadCreator(thead(attributes))
    }

    override fun tbody(attributes: Map<String, Any>): MDLTBodyCreator {
        tbodyUsed = true
        return MDLTBodyCreator(super.tbody(attributes).element)
    }
}

class MDLTHeadCreator(wrapped: TheadCreator) : TheadCreator(wrapped.element) {
    override fun tr(attributes: Map<String, Any>): MDLTrHeadElement = MDLTrHeadElement(tr(attributes).element)
}

class MDLTBodyCreator(wrapped: Element) : TbodyCreator(wrapped) {
    override fun tr(attributes: Map<String, Any>): MDLTrBodyElement = MDLTrBodyElement(tr(attributes).element)
}

class MDLTrHeadElement(wrapped: Element) : TrHeadCreator(wrapped) {
    fun th(
            sort: TableSortOrder = TableSortOrder.none,
            nonNumeric: Boolean = false,
            attributes: Map<String, Any>
    ): MDLThElement = MDLThElement(super.th(attributes
            .classes("mdl-data-table__header--sorted-ascending", onlyIf = sort == TableSortOrder.ascending)
            .classes("mdl-data-table__header--sorted-descending", onlyIf = sort == TableSortOrder.descending)
            .classes("mdl-data-table__cell--non-numeric", onlyIf = nonNumeric)
    ).element)

    override fun th(attributes: Map<String, Any>): MDLThElement {
        return MDLThElement(super.th(attributes).element)
    }
}

class MDLTrBodyElement(wrapped: Element) : TrBodyCreator(wrapped) {
    fun td(
            sort: TableSortOrder = TableSortOrder.none,
            nonNumeric: Boolean = false,
            attributes: Map<String, Any>
    ): MDLTdElement = MDLTdElement(super.td(attributes
            .classes("mdl-data-table__header--sorted-ascending", onlyIf = sort == TableSortOrder.ascending)
            .classes("mdl-data-table__header--sorted-descending", onlyIf = sort == TableSortOrder.descending)
            .classes("mdl-data-table__cell--non-numeric", onlyIf = nonNumeric)
    ).element)

    override fun td(attributes: Map<String, Any>): MDLTdElement {
        return MDLTdElement(super.td(attributes).element)
    }
}

enum class TableSortOrder {
    ascending, descending, none
}

class MDLThElement(wrapped: Element) : ThCreator(wrapped) {

}

class MDLTdElement(wrapped: Element) : TdElement(wrapped) {

}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class MDLTableHeaderName(val name: String)
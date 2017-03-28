package com.github.sanity.kweb.plugins.materialdesignlite.layout

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.attributes.id
import com.github.sanity.kweb.dom.element.creation.DivCreator
import com.github.sanity.kweb.dom.element.creation.insert
import com.github.sanity.kweb.dom.element.modification.text
import com.github.sanity.kweb.plugins.materialdesignlite.MDLCreator

/**
 * See https://getmdl.io/components/index.html#layout-section/tabs
 */

fun MDLCreator.tabs(rippleEffect: Boolean = false, attributes: Map<String, Any> = attr, tabBarAttributes: Map<String, Any> = attr)
        = TabsElement(element.create().div(attributes
        .classes("mdl-tabs", "mdl-js-tabs")
        .classes("mdl-js-ripple-effect", onlyIf = rippleEffect)),
        tabBarAttributes
)

class TabsElement(wrapped: DivCreator, tabBarAttributes: Map<String, Any>) : DivCreator(wrapped) {
    private val tabBar = insert().div(tabBarAttributes.classes("mdl-tabs__tab-bar"))

    fun panel(
            text: String,
            id: String = toId(text),
            isActive: Boolean = false,
            attributes: Map<String, Any> = attr,
            tabAttributes: Map<String, Any> = attr
    ): MDLCreator {
        // TODO: Validate id string
        tabBar.insert().a(href = "#$id", attributes = tabAttributes
                .classes("mdl-tabs__tab")
                .classes("is-active", onlyIf = isActive))
                .setText(text)
        return MDLCreator(insert().div(attributes
                .id(id)
                .classes("mdl-tabs__panel")
                .classes("is-active", onlyIf = isActive)
        ).insert())
    }
}

// TODO: Make this more robust - see http://stackoverflow.com/a/566398/16050
private fun toId(text: String): String = text.toLowerCase().replace(" ", "_")

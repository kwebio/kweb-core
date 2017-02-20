package com.github.sanity.kweb.plugins.materialdesignlite.layout

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.attributes.id
import com.github.sanity.kweb.dom.element.creation.DivElement
import com.github.sanity.kweb.dom.element.creation.a
import com.github.sanity.kweb.dom.element.creation.div
import com.github.sanity.kweb.dom.element.modification.setText
import com.github.sanity.kweb.plugins.materialdesignlite.MDLReceiver

/**
 * See https://getmdl.io/components/index.html#layout-section/tabs
 */

fun MDLReceiver.tabs(rippleEffect: Boolean = false, attributes: Map<String, Any> = attr, tabBarAttributes: Map<String, Any> = attr)
        = TabsElement(parent.div(attributes
        .classes("mdl-tabs", "mdl-js-tabs")
        .classes("mdl-js-ripple-effect", onlyIf = rippleEffect)),
        tabBarAttributes
)

class TabsElement(wrapped: DivElement, tabBarAttributes: Map<String, Any>) : DivElement(wrapped) {
    private val tabBar = div(tabBarAttributes.classes("mdl-tabs__tab-bar"))

    fun panel(
            text: String,
            id: String = toId(text),
            isActive: Boolean = false,
            attributes: Map<String, Any> = attr,
            tabAttributes: Map<String, Any> = attr
    ): MDLReceiver {
        // TODO: Validate id string
        tabBar.a(href = "#$id", attributes = tabAttributes
                .classes("mdl-tabs__tab")
                .classes("is-active", onlyIf = isActive))
                .setText(text)
        return MDLReceiver(div(attributes
                .id(id)
                .classes("mdl-tabs__panel")
                .classes("is-active", onlyIf = isActive)
        ))
    }
}

// TODO: Make this more robust - see http://stackoverflow.com/a/566398/16050
private fun toId(text: String): String = text.toLowerCase().replace(" ", "_")

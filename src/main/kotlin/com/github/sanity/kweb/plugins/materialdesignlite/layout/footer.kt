package com.github.sanity.kweb.plugins.materialdesignlite.layout

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.creation.DivCreator
import com.github.sanity.kweb.dom.element.creation.ElementCreator
import com.github.sanity.kweb.dom.element.creation.button

/**
 * See https://getmdl.io/components/index.html#layout-section/footer
 */

// TODO: megaFooter

fun LayoutCreator.miniFooter(attributes: Map<String, Any> = attr) = MiniFooterCreator(e.footer(attributes.classes("mdl-mini-footer")))

class MiniFooterCreator(wrapped: ElementCreator) : ElementCreator(wrapped.element) {
    fun leftSection(attributes: Map<String, Any> = attr) = MiniFooterSection(div(attributes.classes("mdl-mini-footer__left-section")))

    fun rightSection(attributes: Map<String, Any> = attr) = MiniFooterSection(div(attributes.classes("mdl-mini-footer__right-section")))
}

class MiniFooterSection(wrapped: DivCreator) : DivCreator(wrapped.element) {
    fun logo(attributes: Map<String, Any> = attr) = div(attributes.classes("mdl-logo"))

    fun linkList(attributes: Map<String, Any> = attr) = ul(attributes.classes("mdl-mini-footer__link-list"))

    // TODO: User needs to be able to specify their own social button css classes, see:
    // TODO: https://github.com/google/material-design-lite/blob/88872e672e41c56af0a78a35b34373b8c4a8c49d/docs/_assets/main.css#L533
    // TODO: This might need to wait for a css DSL
    fun socialButton(attributes: Map<String, Any> = attr) = button(attributes = attributes.classes("mdl-mini-footer__social-btn"))
}

package com.github.sanity.kweb.plugins.materialdesignlite.layout

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.creation.*
import com.github.sanity.kweb.plugins.materialdesignlite.MDLElement

/**
 * See https://getmdl.io/components/index.html#layout-section/footer
 */

// TODO: megaFooter

fun MDLElement.miniFooter(attributes: Map<String, Any> = attr) = MiniFooterElement(footer(attributes.classes("mdl-mini-footer")))

class MiniFooterElement(wrapped: FooterElement) : FooterElement(wrapped) {
    fun leftSection(attributes: Map<String, Any> = attr) = MiniFooterSection(div(attributes.classes("mdl-mini-footer__left-section")))

    fun rightSection(attributes: Map<String, Any> = attr) = MiniFooterSection(div(attributes.classes("mdl-mini-footer__right-section")))
}

class MiniFooterSection(wrapped: DivElement) : DivElement(wrapped) {
    fun logo(attributes: Map<String, Any> = attr) = div(attributes.classes("mdl-logo"))

    fun linkList(attributes: Map<String, Any> = attr) = ul(attributes.classes("mdl-mini-footer__link-list"))

    // TODO: User needs to be able to specify their own social button css classes, see:
    // TODO: https://github.com/google/material-design-lite/blob/88872e672e41c56af0a78a35b34373b8c4a8c49d/docs/_assets/main.css#L533
    fun socialButton(attributes: Map<String, Any> = attr) = button(attributes = attributes.classes("mdl-mini-footer__social-btn"))
}

enum class MiniFooterSide {
    left, right
}
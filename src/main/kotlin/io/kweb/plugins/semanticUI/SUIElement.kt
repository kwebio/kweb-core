package io.kweb.plugins.semanticUI

import io.kweb.dom.element.Element

fun Element.setClasses(semanticUIClasses: SemanticUIClasses): Element {
    return this.setClasses(semanticUIClasses["class"].toString())
}
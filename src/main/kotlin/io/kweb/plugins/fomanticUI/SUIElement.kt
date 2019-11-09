package io.kweb.plugins.fomanticUI

import io.kweb.dom.element.Element

fun Element.setClasses(fomanticUIClasses: FomanticUIClasses): Element {
    return this.setClasses(fomanticUIClasses["class"].toString())
}
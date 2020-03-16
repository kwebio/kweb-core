package kweb.plugins.fomanticUI

import kweb.dom.element.Element

fun Element.setClasses(fomanticUIClasses: FomanticUIClasses): Element {
    return this.setClasses(fomanticUIClasses["class"].toString())
}
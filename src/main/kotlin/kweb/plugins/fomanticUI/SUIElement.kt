package kweb.plugins.fomanticUI

import kweb.Element

fun Element.setClasses(fomanticUIClasses: FomanticUIClasses): Element {
    return this.setClasses(fomanticUIClasses["class"].toString())
}
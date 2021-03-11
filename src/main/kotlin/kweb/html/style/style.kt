package kweb.html.style

import kweb.Element

/**
 * Created by ian on 2/12/17.
 */

class StyleReceiver(private val parent: Element) {
    fun setDisplay(value: DisplayValues) {
        parent.callJs("${parent.jsExpression}.style.display=\"$value\";")
    }

    fun setWidth(value: String) {
        parent.callJs("${parent.jsExpression}.style.width=\"$value\";")
    }

    fun remove() {
        parent.removeAttribute("style")
    }

    enum class DisplayValues {
        none, block
    }
}
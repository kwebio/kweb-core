package kweb.html.style

import kweb.Element

/**
 * Created by ian on 2/12/17.
 */

class StyleReceiver(private val parent: Element) {
    fun setDisplay(value: DisplayValues) {
        parent.callJsFunction("document.getElementById({}).style.display = {}", parent.id, value)
    }

    fun setWidth(value: String) {
        parent.callJsFunction("document.getElementById({}).style.width = {}", parent.id, value)
    }

    fun remove() {
        parent.removeAttribute("style")
    }

    enum class DisplayValues {
        none, block
    }
}
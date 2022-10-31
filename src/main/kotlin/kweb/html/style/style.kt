package kweb.html.style

import kotlinx.serialization.json.JsonPrimitive
import kweb.Element

/**
 * Created by ian on 2/12/17.
 */

class StyleReceiver(private val parent: Element) {
    fun setDisplay(value: DisplayValues) {
        parent.browser.callJsFunction(
            "document.getElementById({}).style.display = {}",
            JsonPrimitive(parent.id),
            JsonPrimitive(value.toString())
        )
    }

    fun setWidth(value: String) {
        parent.browser.callJsFunction(
            "document.getElementById({}).style.width = {}",
            JsonPrimitive(parent.id),
            JsonPrimitive(value)
        )
    }

    fun remove() {
        parent.removeAttribute("style")
    }

    enum class DisplayValues {
        none, block
    }
}
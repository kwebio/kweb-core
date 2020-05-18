package kweb.html.events

import kweb.util.KWebDSL

@KWebDSL
class NewOnImmediateReceiver<T: EventGenerator<T>>(internal val source: T) {
    fun event(eventName: String, callback: () -> Unit): T {
        val immediateJS = source.browser.kweb.catchOutbound {
            callback()
        }
        source.addImmediateEventCode(eventName, immediateJS.joinToString(separator = ""))
        return source
    }
}
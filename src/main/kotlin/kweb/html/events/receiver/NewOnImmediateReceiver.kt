package kweb.html.events.receiver

import kweb.KWebDSL

@KWebDSL
class NewOnImmediateReceiver<T: EventGenerator<T>>(private val source: T) {
    fun event(eventName: String, callback: () -> Unit): T {
        val immediateJS = source.browser.kweb.catchOutbound {
            callback()
        }
        source.addImmediateEventCode(eventName, immediateJS.joinToString(separator = ""))
        return source
    }
}
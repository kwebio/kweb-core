package kweb.html.events

import java.util.*

interface EventGenerator<T> {
    fun addEventListener(eventName: String, returnEventFields: Set<String> = Collections.emptySet(), retrieveJs: String?, callback: (Any) -> Unit): T
}
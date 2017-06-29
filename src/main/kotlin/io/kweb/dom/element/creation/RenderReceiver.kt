package io.kweb.dom.element.creation

import io.kweb.dom.element.Element
import io.kweb.state.Watchable
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.properties.Delegates

class RenderReceiver<out E : Element>(private val ec : ElementCreator<E>) {
    fun <T : Any> to(observable: Watchable<T>, d : ElementCreator<E>.(T) -> Unit) {
        // TODO: We have to use the notNull() delegate because listenerId isn't known
        // TODO: until after the call to addListener()
        // TODO: This is ugly, perhaps listenerId should be passed as param to listener
        var listenerId by Delegates.notNull<Long>()
        val lastRenderCleaners = ConcurrentLinkedDeque<Cleaner>()
        listenerId = observable.addListener({ oldState, newState ->
            if (oldState != newState) {
                while (!lastRenderCleaners.isEmpty()) {
                    lastRenderCleaners.poll().invoke()
                }
                ec.onCleanup(true) {
                    observable.removeListener(listenerId)
                }
                ec.withCleanupListener({lastRenderCleaners.add(it)}) {
                    d(ec, newState)
                }
            }
        })
        ec.withCleanupListener({lastRenderCleaners.add(it)}) {
            d(ec, observable.value)
        }
    }
}
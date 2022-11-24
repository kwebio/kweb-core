package kweb.state

import kweb.Element
import kweb.ElementCreator
import kweb.WebBrowser
import kweb.span
import kweb.state.RenderState.*
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.ArrayList
import kotlin.collections.forEach
import kotlin.collections.plusAssign

/**
 * Created by ian on 6/18/17.
 */

private val logger = KotlinLogging.logger {}
object RenderSpanNames{
    const val startMarkerClassName = "rMStart"
    const val endMarkerClassName = "rMEnd"
    const val listStartMarkerClassName = "rLStart"
    const val listEndMarkerClassName = "rLEnd"
}

/**
 * Render the value of a [KVal] into DOM elements, and automatically re-render those
 * elements whenever the value changes.
 */
fun <T : Any?> ElementCreator<*>.render(
    value: KVal<T>,
    block: ElementCreator<Element>.(T) -> Unit
) : RenderFragment {

    val previousElementCreator: AtomicReference<ElementCreator<Element>?> = AtomicReference(null)

    val renderState = AtomicReference(NOT_RENDERING)

    //TODO this could possibly be improved
    val renderFragment: RenderFragment = if (element.browser.isCatchingOutbound() == null) {
        element.browser.batch(WebBrowser.CatcherType.RENDER) {
            val startSpan = span().classes(RenderSpanNames.startMarkerClassName)
            val endSpan = span().classes(RenderSpanNames.endMarkerClassName)
            RenderFragment(startSpan.id, endSpan.id)
        }
    } else {
        val startSpan = span().classes(RenderSpanNames.startMarkerClassName)
        val endSpan = span().classes(RenderSpanNames.endMarkerClassName)
        RenderFragment(startSpan.id, endSpan.id)
    }

    fun eraseBetweenSpans() {
        element.removeChildrenBetweenSpans(renderFragment.startId, renderFragment.endId)
        previousElementCreator.getAndSet(null)?.cleanup()
    }

    fun eraseAndRender() {
        eraseBetweenSpans()

        previousElementCreator.set(ElementCreator<Element>(this.element, this, insertBefore = renderFragment.endId))
        renderState.set(RENDERING_NO_PENDING_CHANGE)
        val elementCreator = previousElementCreator.get()
        if (elementCreator != null) {
            elementCreator.block(value.value)
        } else {
            logger.error("previousElementCreator.get() was null in eraseAndRender()")
            //TODO This warning message could be made more helpful. I can't think of a situation where this could actually happen
            //So I'm not sure what we need to say in this message.
        }
        if (renderState.get() == RENDERING_NO_PENDING_CHANGE) {
            renderState.set(NOT_RENDERING)
        }
    }

    //TODO this function could probably have a clearer name
    //It's purpose is to monitor renderState, and call eraseAndRender() if the page is rendering.
    fun renderLoop() {
        do {
            if (element.browser.isCatchingOutbound() == null) {
                element.browser.batch(WebBrowser.CatcherType.RENDER) {
                    eraseAndRender()
                }
            } else {
                eraseAndRender()
            }
        } while (renderState.get() != NOT_RENDERING)
    }

    val listenerHandle = value.addListener { _, _ ->
        when (renderState.get()) {
            NOT_RENDERING -> {
                renderLoop()
            }
            RENDERING_NO_PENDING_CHANGE -> {
                renderState.set(RENDERING_WITH_PENDING_CHANGE)
            }
            else -> {
                // This space intentionally left blank
            }
        }
    }
    renderFragment.addDeletionListener {
        value.removeListener(listenerHandle)
    }

    //we have to make sure to call renderLoop to start the initial render and begin monitoring renderState
    renderLoop()

    this.onCleanup(false) {
        //TODO I'm not sure what cleanup needs to be done now that there is no container element
    }

    this.onCleanup(true) {
        previousElementCreator.getAndSet(null)?.cleanup()
        value.removeListener(listenerHandle)
    }

    return renderFragment
}

fun ElementCreator<*>.closeOnElementCreatorCleanup(kv: KVal<*>) {
    this.onCleanup(withParent = true) {
        kv.close(CloseReason("Closed because a parent ElementCreator was cleaned up"))
    }
}

/**
 * Typealias for [ElementCreator] to simply create and manage components using an
 * extension function
 */
typealias Component = ElementCreator<*>

class RenderFragment(val startId: String, val endId: String) {
    private val deletionListeners = ArrayList<() -> Unit>()

    internal fun addDeletionListener(listener: () -> Unit) {
        synchronized(deletionListeners) {
            deletionListeners += listener
        }
    }

    fun delete() {
        synchronized(deletionListeners) {
            deletionListeners.forEach { it.invoke() }
        }
    }
}

class RenderHandle<ITEM : Any>(val renderFragment: RenderFragment, val kvar: KVar<ITEM>)

private enum class RenderState {
    NOT_RENDERING, RENDERING_NO_PENDING_CHANGE, RENDERING_WITH_PENDING_CHANGE
}
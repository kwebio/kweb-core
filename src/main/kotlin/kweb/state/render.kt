package kweb.state

import kweb.Element
import kweb.ElementCreator
import kweb.WebBrowser
import kweb.span
import kweb.state.RenderState.*
import mu.two.KotlinLogging
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val logger = KotlinLogging.logger {}
object RenderSpanNames{
    const val startMarkerClassName = "rMStart"
    const val endMarkerClassName = "rMEnd"
    const val listStartMarkerClassName = "rLStart"
    const val listEndMarkerClassName = "rLEnd"
}

fun <T : Any?> ElementCreator<*>.render(
    value: KVal<T>,
    block: ElementCreator<Element>.(T) -> Unit
) : RenderFragment {
    val previousElementCreatorLock = ReentrantLock()
    val previousElementCreator: AtomicReference<ElementCreator<Element>?> = AtomicReference(null)
    val renderStateLock = ReentrantLock()
    val renderState = AtomicReference(NOT_RENDERING)
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
        previousElementCreatorLock.withLock {
            previousElementCreator.getAndSet(null)?.cleanup()
        }
    }

    fun eraseAndRender() {
        eraseBetweenSpans()
        previousElementCreatorLock.withLock {
            previousElementCreator.set(ElementCreator(this.element, this, insertBefore = renderFragment.endId))
        }
        renderStateLock.withLock {
            renderState.set(RENDERING_NO_PENDING_CHANGE)
        }
        val elementCreator = previousElementCreator.get()
        elementCreator?.let {
            it.block(value.value ?: return@let)
        } ?: run {
            logger.error("previousElementCreator.get() was null in eraseAndRender()")
        }
        renderStateLock.withLock {
            if (renderState.get() == RENDERING_NO_PENDING_CHANGE) {
                renderState.set(NOT_RENDERING)
            }
        }
    }

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
                renderStateLock.withLock {
                    renderState.set(RENDERING_WITH_PENDING_CHANGE)
                }
            }
            else -> {
                // This space intentionally left blank
            }
        }
    }

    renderFragment.addDeletionListener {
        value.removeListener(listenerHandle)
    }

    renderLoop()

    this.onCleanup(true) {
        value.removeListener(listenerHandle)
        previousElementCreatorLock.withLock {
            previousElementCreator.getAndSet(null)?.cleanup()
        }
    }

    return renderFragment
}

fun ElementCreator<*>.closeOnElementCreatorCleanup(kv: KVal<*>) {
    this.onCleanup(withParent = true) {
        kv.close(CloseReason("Closed because a parent ElementCreator was cleaned up"))
    }
}

// Deprecated methods left as they are

class RenderFragment(val startId: String, val endId: String) {
    private val deletionListeners = mutableListOf<() -> Unit>()

    internal fun addDeletionListener(listener: () -> Unit) {
        synchronized(deletionListeners) {
            deletionListeners += listener
        }
    }

    fun delete() {
        synchronized(deletionListeners) {
            deletionListeners.forEach { it.invoke() }
            deletionListeners.clear()
        }
    }
}

private enum class RenderState {
    NOT_RENDERING, RENDERING_NO_PENDING_CHANGE, RENDERING_WITH_PENDING_CHANGE
}

class RenderHandle<ITEM : Any>(val renderFragment: RenderFragment, val kvar: KVar<ITEM>)

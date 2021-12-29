package kweb.state

import kotlinx.serialization.json.JsonPrimitive
import kweb.*
import kweb.shoebox.KeyValue
import kweb.shoebox.OrderedViewSet
import kweb.shoebox.Shoebox
import kweb.state.RenderState.*
import kweb.util.random
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by ian on 6/18/17.
 */

private val logger = KotlinLogging.logger {}

fun <T : Any?> ElementCreator<*>.render(
    value: KVal<T>,
    block: ElementCreator<Element>.(T) -> Unit
) : RenderFragment {

    val previousElementCreator: AtomicReference<ElementCreator<Element>?> = AtomicReference(null)

    val renderState = AtomicReference(NOT_RENDERING)

    //TODO this could possibly be improved
    val renderFragment: RenderFragment = if (parent.browser.isCatchingOutbound() == null) {
        parent.browser.batch(WebBrowser.CatcherType.RENDER) {
            val startSpan = span().classes("RenderMarkerStart")
            val endSpan = span().classes("RenderMarkerEnd")
            RenderFragment(startSpan.id, endSpan.id)
        }
    } else {
        val startSpan = span().classes("RenderMarkerStart")
        val endSpan = span().classes("RenderMarkerEnd")
        RenderFragment(startSpan.id, endSpan.id)
    }

    fun eraseBetweenSpans() {
        parent.removeChildrenBetweenSpans(renderFragment.startId, renderFragment.endId)
        previousElementCreator.getAndSet(null)?.cleanup()
    }

    fun eraseAndRender() {
        eraseBetweenSpans()

        previousElementCreator.set(ElementCreator<Element>(this.parent, this, insertBefore = renderFragment.endId))
        renderState.set(RENDERING_NO_PENDING_CHANGE)
        previousElementCreator.get()!!.block(value.value) // TODO: Refactor to remove !!
        if (renderState.get() == RENDERING_NO_PENDING_CHANGE) {
            renderState.set(NOT_RENDERING)
        }
    }

    //TODO rename this function
    fun renderLogic() {
        do {
            if (parent.browser.isCatchingOutbound() == null) {
                parent.browser.batch(WebBrowser.CatcherType.RENDER) {
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
                renderLogic()
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

    previousElementCreator.set(ElementCreator<Element>(this.parent, this, insertBefore = renderFragment.endId))
    renderState.set(RENDERING_NO_PENDING_CHANGE)
    previousElementCreator.get()!!.block(value.value) // TODO: Refactor to remove !!
    if (renderState.get() == RENDERING_NO_PENDING_CHANGE) {
        renderState.set(NOT_RENDERING)
    }

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

fun <T : Any> ElementCreator<*>.toVar(shoebox: Shoebox<T>, key: String): KVar<T> {
    val value = shoebox[key] ?: throw NoSuchElementException("Key $key not found")
    val w = KVar(value)
    w.addListener { _, n ->
        require(this.browser.isCatchingOutbound() != WebBrowser.CatcherType.IMMEDIATE_EVENT) {
            """You appear to be modifying Shoebox state from within an onImmediate callback, which
                |should only make simple modifications to the DOM.""".trimMargin()
        }
        shoebox[key] = n
    }
    val changeHandle = shoebox.onChange(key) { _, n, _ -> w.value = n }
    w.onClose { shoebox.deleteChangeListener(key, changeHandle) }
    this.onCleanup(withParent = true) {
        w.close(CloseReason("Closed because parent ElementCreator was cleaned up"))
    }
    return w
}


private data class ItemInfo<ITEM : Any>(val creator: ElementCreator<Element>, val KVar: KVar<ITEM>)

class RenderFragment(val startId: String, val endId : String) {
    private val deletionListeners = ArrayList<() -> Unit>()

    internal fun addDeletionListener(listener : () -> Unit) {
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

class RenderHandle<ITEM : Any>(val renderFragment: RenderFragment, val kvar : KVar<ITEM>)

data class IndexedItem<I>(val index: Int, val total: Int, val item: I)

/* Possible APIs for renderEach
 * Need to do a "diff" on `items` to figure out what has been added/removed
 */
/*fun <ITEM : Any, EL : Element> ElementCreator<EL>.renderEach1(
    items: KVal<Collection<ITEM>>,
    renderer: ElementCreator<EL>.(KVal<ITEM>) -> Unit
) {
    TODO()
}

*//* Candidate #2
 * We pass an ITEM in to the renderer rather than a KVal<ITEM>, so now diff needs to identify changed elements too
*//*
fun <ITEM : Any, EL : Element> ElementCreator<EL>.renderEach2(
    items: KVal<Collection<ITEM>>,


    renderer: ElementCreator<EL>.(ITEM) -> Unit
) {
    TODO()
}*/

/* Candidate #3
 * Rather than a normal Collection wrapped in a KVal we pass some kind of observable collection, similar to
 * OrderedViewSet but not tied to Shoebox.
 *
 * PROS:
 * * No expensive diff for large collections
 */

class ObservableList<ITEM : Any>(val initialItems : MutableList<ITEM>) {

    val items = ArrayList(initialItems)

    private val listeners = ConcurrentHashMap<Long, (List<Modification<ITEM>>) -> Unit>()

    fun insert(position : Int, item : ITEM) = applyModifications(listOf(Modification.Insertion(position, item)))
    fun change(position : Int, newItem : ITEM) = applyModifications(listOf(Modification.Change(position, newItem)))
    fun move(oldPosition: Int, newPosition: Int) = applyModifications(listOf(Modification.Move(oldPosition, newPosition)))
    fun delete(position : Int) = applyModifications(listOf(Modification.Deletion<ITEM>(position)))

    fun applyModifications(modifications : List<Modification<ITEM>>) {
        synchronized(items) {
            for (change in modifications) {
                when(change) {
                    is Modification.Change -> {
                        items[change.position] = change.newItem
                    }
                    is Modification.Deletion -> {
                        items.removeAt(change.position)
                    }
                    is Modification.Insertion -> {
                        items.add(change.position, change.item)
                    }
                    is Modification.Move -> {
                        if (change.oldPosition == change.newPosition) {
                            continue
                        }
                        val item = items[change.oldPosition]
                        if (change.oldPosition > change.newPosition) {
                            items.add(change.newPosition, item)
                            items.removeAt(change.oldPosition+1)
                        }
                        else { //change.newPosition > change.oldPosition
                            items.removeAt(change.oldPosition)
                            items.add(change.newPosition, item)
                        }
                    }
                }
            }
        }
        listeners.values.forEach { it(modifications) }
    }

    sealed class Modification<ITEM> {
        class Insertion<ITEM>(val position : Int, val item : ITEM) : Modification<ITEM>()
        class Change<ITEM>(val position : Int, val newItem : ITEM) : Modification<ITEM>()
        class Move<ITEM>(val oldPosition : Int, val newPosition : Int) : Modification<ITEM>()
        class Deletion<ITEM>(val position : Int) : Modification<ITEM>()
    }

    fun addListener(changes : (List<Modification<ITEM>>) -> Unit) : Long {
        val handle = random.nextLong()
        listeners[handle] = changes
        return handle
    }

    fun removeListener(handle : Long) {
        listeners.remove(handle)
    }
}

/*




   <span START-1>
   1
   <span END-1>
   <span START-2>
   2
   <span END-2>

   // to insert something between 1 and 2 create new ElementCreator where insertBefore is <span START-2>, then call
   // render ON THIS ELEMENT CREATOR. NOTE these new ElementCreators may require cleanup
 */


/*
fun <ITEM : Any, EL : Element> ElementCreator<EL>.renderEachWIP(
    itemCollection: KVal<out Collection<ITEM>>,
    itemRenderer: ElementCreator<EL>.(ITEM) -> Unit
) {

    val observableList : ObservableList<ITEM> = ObservableList(itemCollection.value.toList())

    renderEachWIP(observableList, itemRenderer)

    // Listen for changes to the collection
    val collectionListenerHandle = itemCollection.addListener { old, new ->
        // Do diff on collection then apply modifications to ObservableList which will be
        // rendered by lower-level renderEach

            val diff : List<ObservableList.Modification<ITEM>> = listOf() // <--- do diff between old and new

            observableList.applyModifications(diff)

    }

    this.onCleanup(true) {
        itemCollection.removeListener(collectionListenerHandle)
    }
}
*/

/// Lower level interface to renderEach
fun <ITEM : Any, EL : Element> ElementCreator<EL>.renderEachWIP(
    observableList: ObservableList<ITEM>,
    itemRenderer: ElementCreator<Element>.(ITEM) -> Unit
) {

    val listFragment = RenderFragment(
        span().classes("RenderListStart").id,
        span().classes("RenderListEnd").id
    )

    fun insertItem(position: Int, newItem: ITEM,
                   renderHandles: ArrayList<RenderHandle<ITEM>>,) {
        val nextElementRenderMarkerStartId: String = if (position == renderHandles.size) {
            listFragment.endId
        } else {
            renderHandles[position].renderFragment.startId
        }
        val itemElementCreator =
            ElementCreator<Element>(this.parent, this, nextElementRenderMarkerStartId)
        val kvar = KVar(newItem)
        val newFragment = itemElementCreator.render(kvar) { item ->
            itemRenderer(item)
        }
        renderHandles.add(position, RenderHandle(newFragment, kvar))
    }

    fun <ITEM : Any> deleteItem(position: Int, renderHandles: ArrayList<RenderHandle<ITEM>>) {
        val renderHandleToRemove = renderHandles[position].renderFragment
        renderHandles.removeAt(position)
        browser.callJsFunction("""
            var start_id = {};
            var end_id = {};
            var start_element = document.getElementById(start_id);
            var end_element = document.getElementById(end_id);
            var parent = start_element.parentNode;
            while (start_element.nextSibling != end_element) {
                parent.removeChild(start_element.nextSibling);
            }
            parent.removeChild(start_element);
            parent.removeChild(end_element);
            """.trimIndent(), JsonPrimitive(renderHandleToRemove.startId),
            JsonPrimitive(renderHandleToRemove.endId))
        renderHandleToRemove.delete()
    }

    ElementCreator<Element>(this.parent, this.parentCreator, insertBefore = listFragment.endId).apply {

        // TODO: These renderFragments must be kept in sync with the items in observableList that they're rendering
        // TODO: We should also store the KVar<ITEM>
        val renderHandles = ArrayList<RenderHandle<ITEM>>()

    synchronized(renderHandles) {
        //render the initial observableList to the DOM storing the Handles in renderHandles
        for (item in observableList.getItems()) {
            val kvar = KVar(item)
            val fragment = render(kvar) { fragItem ->
                itemRenderer(fragItem)
            }
            renderHandles += RenderHandle(fragment, kvar)
        }
    }

    val handle = observableList.addListener { changes ->
        synchronized(renderHandles) {
            for (change in changes) {
                // Apply change to DOM using renderHandles, and update renderHandles to keep it in sync with observableList
                when(change) {
                    is ObservableList.Modification.Change -> {
                        // TODO: Don't do a new render, get the KVar<ITEM> from `renderHandles` and update it,
                        // TODO: render() should take care of the rest
                        val kvar = KVar(change.newItem)





                        renderHandles[change.position].kvar.value = kvar.value
                    }
                    is ObservableList.Modification.Deletion -> {
                        val kvar = KVar(change.position)
                        renderHandles[change.position].renderFragment.delete()
                        renderHandles.removeAt(change.position)
                    }
                    is ObservableList.Modification.Insertion -> {
                        val kvar = KVar(change.item)
                        val newFragment = render(kvar) { item ->
                            this@renderEachWIP.itemRenderer(item)
                        }
                        renderHandles.add(change.position, RenderHandle(newFragment, kvar))
                    }
                    is ObservableList.Modification.Move -> {
                        if (change.oldPosition == change.newPosition) {
                            continue
                        }
                        val kvar = KVar(observableList.getItems()[change.oldPosition])
                        if (change.oldPosition > change.newPosition) {
                            renderHandles[change.newPosition].kvar.value = kvar.value
                            renderHandles.removeAt(change.oldPosition + 1)
                        }
                        if (change.newPosition > change.oldPosition) {
                            renderHandles.removeAt(change.oldPosition)
                            renderHandles[change.newPosition].kvar.value = kvar.value
                        }
                    }
                }
            }
        }
    }

    onCleanup(true) {
        observableList.removeListener(handle)
    }
}

/*fun <ITEM : Any> insertItem(elementCreator: ElementCreator<Element>,
                            change : ObservableList.Modification.Insertion<ITEM>,
                            renderHandles: ArrayList<RenderHandle<ITEM>>,
                            ) {
    val nextElementRenderMarkerStartId: String = if (change.position == renderHandles.size) {
        listFragment.endId
    } else {
        renderHandles[change.position].renderFragment.startId
    }
    val itemElementCreator =
        ElementCreator<Element>(this.parent, this, nextElementRenderMarkerStartId)
    val kvar = KVar(change.item)
    val newFragment = itemElementCreator.render(kvar) { item ->
        itemRenderer(item)
    }
    renderHandles.add(change.position, RenderHandle(newFragment, kvar))
}*/

/**
 *
 *
 * // @sample ordered_view_set_sample
 */
fun <ITEM : Any, EL : Element> ElementCreator<EL>.renderEach(
    orderedViewSet: OrderedViewSet<ITEM>,
    renderer: ElementCreator<EL>.(KVar<ITEM>) -> Unit
) {
    val items = CopyOnWriteArrayList<ItemInfo<ITEM>>()
    for (keyValue in orderedViewSet.keyValueEntries) {
        items += createItem(orderedViewSet, keyValue, renderer, insertAtPosition = null)
    }

    val onInsertHandler = orderedViewSet.onInsert { index, inserted ->
        if (index < items.size) {
            items.add(index, createItem(orderedViewSet, inserted, renderer, "index"))//TODO I broke this line so the project would compile
        } else {
            items.add(createItem(orderedViewSet, inserted, renderer, insertAtPosition = null))
        }
    }
    this.onCleanup(true) { orderedViewSet.deleteInsertListener(onInsertHandler) }

    val onRemoveHandler = orderedViewSet.onRemove { index, keyValue ->
        if (index >= items.size) {
            logger.warn("Invalid index $index to retrieve item from items list of size ${items.size} for key ${keyValue.key} and item ${keyValue.value}, ignoring.")
        } else {
            val removed = items.removeAt(index)
            removed.creator.cleanup()
            removed.KVar.close(CloseReason("Closed because associated item was removed from OrderedViewSet"))
        }
    }

    this.onCleanup(true) {

        orderedViewSet.deleteRemoveListener(onRemoveHandler)
    }
}

private fun <ITEM : Any, EL : Element> ElementCreator<EL>.createItem(
    orderedViewSet: OrderedViewSet<ITEM>,
    keyValue: KeyValue<ITEM>,
    renderer: ElementCreator<EL>.(KVar<ITEM>) -> Unit,
    insertAtPosition: String?
)
        : ItemInfo<ITEM> {
    val itemElementCreator = ElementCreator(this.parent, this, insertBefore = insertAtPosition)
    val itemVar = itemElementCreator.toVar(orderedViewSet.view.viewOf, keyValue.key)
    try {
        renderer(itemElementCreator, itemVar)
    } catch (e: Exception) {
        logger.error("Error rendering item", e)
    }

    if (itemElementCreator.elementsCreatedCount > 1) {
        /*
         * Only one element may be created per-item because otherwise it would be much more complicated to figure
         * out where new items should be inserted by the onInsert handler below.  onRemove would be easier because
         * we could just call itemElementCreator.cleanup() to delete that item's elements.
         *
         * This shouldn't be an onerous requirement because typically with lists of things there is just one
          * root <ol> or <ul> per item.  If it does turn out to be a problem we'll need to find another approach.
         */
        error(
            """
            Only one element may be created per item but ${itemElementCreator.elementsCreatedCount} were created for
            item key ${keyValue.key}.  Note that this element may have as many children as you like, so you may just need
            to wrap the elements in a <DIV> or other element type.
""".trimIndent()
        )
    }
    return ItemInfo(itemElementCreator, itemVar)
}

private enum class RenderState {
    NOT_RENDERING, RENDERING_NO_PENDING_CHANGE, RENDERING_WITH_PENDING_CHANGE
}

private fun ordered_view_set_sample() {
    data class Cat(val name: String, val color: String)

    val cats = Shoebox<Cat>()
    val catColorView = cats.view("catColors", Cat::color)
    Kweb(port = 1234, buildPage = {
        doc.body.new {
            renderEach(catColorView.orderedSet("brown")) { brownCat ->
                div().new {
                    h1().text(brownCat.map(Cat::name))
                }
            }
        }
    })
}


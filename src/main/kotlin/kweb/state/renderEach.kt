package kweb.state

import kotlinx.serialization.json.JsonPrimitive
import kweb.Element
import kweb.ElementCreator
import kweb.span
import java.util.concurrent.ConcurrentHashMap

class ObservableList<ITEM : Any>(
    initialItems: List<ITEM>,
    override val size: Int = initialItems.size
) : MutableList<ITEM>{

    private val items = ArrayList(initialItems)
    fun getItems(): ArrayList<ITEM> {
        synchronized(items) {
            return ArrayList(items)
        }
    }

    private val listeners = ConcurrentHashMap<Long, (List<Modification<ITEM>>) -> Unit>()
    private fun insert(position: Int, item: ITEM) = applyModifications(listOf(Modification.Insertion(position, item)))
    private fun change(position: Int, newItem: ITEM) = applyModifications(listOf(Modification.Change(position, newItem)))
    private fun delete(position: Int) = applyModifications(listOf(Modification.Deletion(position)))

    fun move(oldPosition: Int, newPosition: Int) =
        applyModifications(listOf(Modification.Move(oldPosition, newPosition)))

    fun applyModifications(modifications: List<Modification<ITEM>>) {
        synchronized(items) {
            for (change in modifications) {
                when (change) {
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
                        if (change.oldPosition >= change.newPosition) {
                            items.add(change.newPosition, item)
                            items.removeAt(change.oldPosition + 1)
                            println("Items size: ${items.size}")
                        } else { //change.newPosition > change.oldPosition
                            items.removeAt(change.oldPosition)
                            items.add(change.newPosition, item)
                            println("Items size: ${items.size}" +
                                    "\nItems Contents: $items")
                        }
                    }
                }
            }
        }
        listeners.values.forEach { it(modifications) }
    }

    sealed class Modification<ITEM> {
        class Insertion<ITEM>(val position: Int, val item: ITEM) : Modification<ITEM>()
        class Change<ITEM>(val position: Int, val newItem: ITEM) : Modification<ITEM>()
        class Move<ITEM>(val oldPosition: Int, val newPosition: Int) : Modification<ITEM>()
        class Deletion<ITEM>(val position: Int) : Modification<ITEM>()
    }

    fun addListener(changes: (List<Modification<ITEM>>) -> Unit): Long {
        val handle = kweb.util.random.nextLong()
        listeners[handle] = changes
        return handle
    }

    fun removeListener(handle: Long) {
        listeners.remove(handle)
    }

    override fun contains(element: ITEM): Boolean {
        return items.contains(element)
    }

    override fun containsAll(elements: Collection<ITEM>): Boolean {
        return items.containsAll(elements)
    }

    override fun get(index: Int): ITEM {
        return items[index]
    }

    override fun indexOf(element: ITEM): Int {
        return items.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return items.isEmpty()
    }

    override fun iterator(): MutableIterator<ITEM> {
        return items.iterator()
    }

    override fun lastIndexOf(element: ITEM): Int {
        return items.lastIndexOf(element)
    }

    override fun add(element: ITEM): Boolean {
        insert(items.size, element)
        return true
    }

    override fun add(index: Int, element: ITEM) {
        insert(index, element)
    }

    override fun addAll(index: Int, elements: Collection<ITEM>): Boolean {
        for ((i, element) in elements.withIndex()) {
            insert(index + i, element)
        }
        return true
    }

    override fun addAll(elements: Collection<ITEM>): Boolean {
        elements.forEach { element ->
            insert(items.size, element)
        }
        return true
    }

    override fun clear() {
        while(items.size > 0) {
            delete(0)
        }
    }

    override fun listIterator(): MutableListIterator<ITEM> {
        return items.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<ITEM> {
        return items.listIterator(index)
    }

    override fun remove(element: ITEM): Boolean {
        val position = items.indexOf(element)
        return if (position == -1) {
            false
        } else {
            delete(position)
            true
        }
    }

    override fun removeAll(elements: Collection<ITEM>): Boolean {
        return if (containsAll(elements)) {
            items.clear()
            true
        } else {
            false
        }
    }

    override fun removeAt(index: Int): ITEM {
        val itemToRemove = items[index]
        delete(index)
        return itemToRemove
    }

    override fun retainAll(elements: Collection<ITEM>): Boolean {
        items.clear()
        addAll(elements)
        return true
    }

    override operator fun set(index: Int, element: ITEM): ITEM {
        val previousElement = items[index]
        change(index, element)
        return previousElement
    }

    //TODO: Documentation will need a note explaining that any modifications to the returned sublist will not propagate in the DOM
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<ITEM> {
        return items.subList(fromIndex, toIndex)
    }
}

fun <ITEM : Any, EL : Element> ElementCreator<EL>.renderEach(
    observableList: ObservableList<ITEM>,
    itemRenderer: ElementCreator<Element>.(ITEM) -> Unit
) {

    //a wrapper made of 2 empty spans around the list of items that renderEach will draw
    //This is needed to be able to use insertBefore on the end span to add items to the end of the list.
    val listFragment = RenderFragment(
        span().classes(RenderSpanNames.listStartMarkerClassName).id,
        span().classes(RenderSpanNames.listEndMarkerClassName).id
    )

    fun insertItem(position: Int, newItem: ITEM,
                   renderHandles: ArrayList<RenderHandle<ITEM>>) {
        val nextElementRenderMarkerStartId: String = if (position == renderHandles.size) {
            listFragment.endId
        } else {
            renderHandles[position].renderFragment.startId
        }
        val itemElementCreator =
            ElementCreator<Element>(this.element, this, nextElementRenderMarkerStartId)
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
            JsonPrimitive(renderHandleToRemove.endId)
        )
        renderHandleToRemove.delete()
    }

    fun moveItemClientSide(itemStartMarker : String, itemEndMarker : String, newPosMarker : String) {
        //This JavaScript takes all elements from one start span to another, denoted by startMarker and endMarker,
        //and inserts them before the element that's ID is passed to the 'newPos' variable.
        //language=JavaScript
        val moveItemCode = """
            var startMarker = document.getElementById({});
            var endMarker = document.getElementById({});
            var elementsToMove = [];
            var currentElement = startMarker.nextSibling;
            while(currentElement !== endMarker) {
                elementsToMove.push(currentElement);
                currentElement = currentElement.nextSibling;
            }
            var newPos = document.getElementById({});
            var listParent = startMarker.parentNode;
            listParent.insertBefore(startMarker, newPos);
            listParent.insertBefore(endMarker, newPos);
            elementsToMove.forEach(function (item){
                listParent.insertBefore(item, endMarker);
            });
            """.trimIndent()
        browser.callJsFunction(moveItemCode, JsonPrimitive(itemStartMarker),
            JsonPrimitive(itemEndMarker), JsonPrimitive(newPosMarker)
        )
    }

    ElementCreator<Element>(this.element, this.parentCreator, insertBefore = listFragment.endId).apply {

        //These renderFragments must be kept in sync with the items in observableList that they're rendering
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
                // TODO: Consider replacing change in changes with, "mods in modifications", to remove confusion between change, and Modification.Change
                for (change in changes) {
                    // Apply change to DOM using renderHandles, and update renderHandles to keep it in sync with observableList
                    when (change) {
                        is ObservableList.Modification.Change -> {
                            renderHandles[change.position].kvar.value = KVar(change.newItem).value
                        }
                        is ObservableList.Modification.Deletion -> {
                            deleteItem(change.position, renderHandles)
                        }
                        is ObservableList.Modification.Insertion -> {
                            insertItem(change.position, change.item, renderHandles)
                        }
                        is ObservableList.Modification.Move -> {
                            if (change.oldPosition == change.newPosition) {
                                continue
                            }
                            if (change.oldPosition > change.newPosition) {
                                moveItemClientSide(renderHandles[change.oldPosition].renderFragment.startId,
                                    renderHandles[change.oldPosition].renderFragment.endId,
                                    renderHandles[change.newPosition].renderFragment.startId)
                                renderHandles.add(change.newPosition, RenderHandle(renderHandles[change.oldPosition].renderFragment, renderHandles[change.oldPosition].kvar))
                                renderHandles.removeAt(change.oldPosition+1)
                            }
                            else { //change.newPosition > change.oldPosition
                                val newRenderHandle = RenderHandle(renderHandles[change.oldPosition].renderFragment, renderHandles[change.oldPosition].kvar)
                                val startId = if (change.newPosition == renderHandles.size-1) {
                                    listFragment.endId
                                } else {
                                    renderHandles[change.newPosition+1].renderFragment.startId
                                }
                                moveItemClientSide(renderHandles[change.oldPosition].renderFragment.startId,
                                    renderHandles[change.oldPosition].renderFragment.endId,
                                    startId)
                                if (change.newPosition == renderHandles.size-1) {
                                    renderHandles.add(newRenderHandle)
                                } else {
                                    renderHandles.add(change.newPosition+1, newRenderHandle)
                                }
                                renderHandles.removeAt(change.oldPosition)
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
}

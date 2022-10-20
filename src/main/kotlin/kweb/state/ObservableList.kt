package kweb.state

import java.util.concurrent.ConcurrentHashMap

/**
 * A list of items that can be observed for changes like [add], [remove], [set], etc. Typically passed to
 * [renderEach].
 */
class ObservableList<ITEM : Any>(
    initialItems: List<ITEM> = emptyList(),
) : MutableList<ITEM>, AutoCloseable {

    private val items = ArrayList(initialItems)
    fun getItems(): ArrayList<ITEM> {
        synchronized(items) {
            return ArrayList(items)
        }
    }

    private val listeners = ConcurrentHashMap<Long, (Iterable<Modification<ITEM>>) -> Unit>()

    private val closeListeners = HashMap<Long, () -> Unit>()
    private fun insert(position: Int, item: ITEM) = applyModifications(listOf(Modification.Insertion(position, item)))
    private fun change(position: Int, newItem: ITEM) =
        applyModifications(listOf(Modification.Change(position, newItem)))

    private fun delete(position: Int) = applyModifications(listOf(Modification.Deletion(position)))

    override val size: Int get() = items.size

    fun move(oldPosition: Int, newPosition: Int) =
        applyModifications(listOf(Modification.Move(oldPosition, newPosition)))

    fun applyModifications(modifications: Iterable<Modification<ITEM>>) {
        if (closed) {
            throw IllegalStateException("Cannot modify closed ObservableList")
        }
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
                            println(
                                "Items size: ${items.size}" +
                                        "\nItems Contents: $items"
                            )
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

    fun addListener(changes: (Iterable<Modification<ITEM>>) -> Unit): Long {
        if (closed) {
            throw IllegalStateException("Cannot add listener to closed ObservableList")
        }
        val handle = kweb.util.random.nextLong()
        listeners[handle] = changes
        return handle
    }

    fun removeListener(handle: Long) {
        if (closed) {
            throw IllegalStateException("Cannot remove listener from closed ObservableList")
        }
        listeners.remove(handle)
    }

    override fun contains(element: ITEM): Boolean {
        if (closed) {
            throw IllegalStateException("Cannot read closed ObservableList")
        }
        return items.contains(element)
    }

    override fun containsAll(elements: Collection<ITEM>): Boolean {
        if (closed) {
            throw IllegalStateException("Cannot read closed ObservableList")
        }
        return items.containsAll(elements)
    }

    override fun get(index: Int): ITEM {
        if (closed) {
            throw IllegalStateException("Cannot read closed ObservableList")
        }
        return items[index]
    }

    override fun indexOf(element: ITEM): Int {
        if (closed) {
            throw IllegalStateException("Cannot read closed ObservableList")
        }
        return items.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        if (closed) {
            throw IllegalStateException("Cannot read closed ObservableList")
        }
        return items.isEmpty()
    }

    override fun iterator(): MutableIterator<ITEM> {
        if (closed) {
            throw IllegalStateException("Cannot read closed ObservableList")
        }
        return items.iterator()
    }

    override fun lastIndexOf(element: ITEM): Int {
        if (closed) {
            throw IllegalStateException("Cannot read closed ObservableList")
        }
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
        while (items.size > 0) {
            delete(0)
        }
    }

    override fun listIterator(): MutableListIterator<ITEM> {
        if (closed) {
            throw IllegalStateException("Cannot read closed ObservableList")
        }
        return items.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<ITEM> {
        if (closed) {
            throw IllegalStateException("Cannot read closed ObservableList")
        }
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
        var removed = false
        for (element in elements) {
            if (remove(element)) {
                removed = true
            }
        }
        return removed
    }

    override fun removeAt(index: Int): ITEM {
        if (closed) {
            throw IllegalStateException("Cannot modify closed ObservableList")
        }
        val itemToRemove = items[index]
        delete(index)
        return itemToRemove
    }

    override fun retainAll(elements: Collection<ITEM>): Boolean {
        if (closed) {
            throw IllegalStateException("Cannot modify closed ObservableList")
        }
        items.clear()
        addAll(elements)
        return true
    }

    override operator fun set(index: Int, element: ITEM): ITEM {
        if (closed) {
            throw IllegalStateException("Cannot set element of closed ObservableList")
        }

        val previousElement = items[index]
        change(index, element)
        return previousElement
    }

    //TODO: Documentation will need a note explaining that any modifications to the returned sublist will not propagate in the DOM
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<ITEM> {
        return items.subList(fromIndex, toIndex)
    }

    private @Volatile
    var closed = false

    fun addCloseListener(listener: () -> Unit): Long {
        val handle = kweb.util.random.nextLong()
        synchronized(closeListeners) {
            closeListeners[handle] = listener
        }
        return handle
    }

    fun removeCloseListener(handle: Long) {
        synchronized(closeListeners) {
            closeListeners.remove(handle)
        }
    }

    override fun close() {
        if (!closed) {
            closed = true
            synchronized(closeListeners) {
                closeListeners.values.forEach { it() }
            }
        }
    }
}
package kweb.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class CallbackOwner(val label : String, val parent : CallbackOwner? = null) {
    private val closeListeners = ConcurrentLinkedQueue<() -> Unit>()
    private val closeMaps = ConcurrentLinkedQueue<Pair<MutableMap<Long, *>, Long>>()

    fun onClose(map : MutableMap<Long, *>, handle : Long) {
        closeMaps.add(map to handle)
    }

    fun onClose(cb : () -> Unit) {
        closeListeners.add(cb)
    }

    fun close() {
        closeListeners.forEach { it() }
        closeListeners.clear()
        closeMaps.forEach { (map, handle) ->
            map.remove(handle)
        }
        closeMaps.clear()
    }

    fun child(label : String) : CallbackOwner {
        val child = CallbackOwner(label, this)
        onClose {
            child.close()
        }
        return child
    }
}

class CallbackMap<L>(private val owner : CallbackOwner) {
    private val map = ConcurrentHashMap<Long, L>()

    fun addListener(listener : L) : Long {
        val handle = random.nextLong()
        map[handle] = listener
        owner.onClose(map, handle)
        return handle
    }

    fun removeListener(handle : Long) {
        map.remove(handle)
    }

    fun clear() {
        map.clear()
    }

    val listeners : Collection<L> get() = map.values
}
package kweb.demos.todo

import kotlinx.serialization.Serializable
import kweb.shoebox.Shoebox
import kweb.shoebox.stores.DirectoryStore
import kweb.shoebox.stores.MemoryStore
import java.nio.file.Files
import java.nio.file.Path

/**
 * Stores all persistent state for this app in a directory, creating it if necessary
 */

class ToDoState() {

    @Serializable data class List(val uid: String, val title: String)

    @Serializable data class Item(val uid: String, val created : Long, val listUid: String, val text: String)

    val lists = Shoebox(MemoryStore<List>())

    val items = Shoebox(MemoryStore<Item>())

    private val itemsByList = items.view("itemsByList", Item::listUid)

    fun itemsByList(listUid: String) = itemsByList.orderedSet(listUid, compareBy(Item::created))
}
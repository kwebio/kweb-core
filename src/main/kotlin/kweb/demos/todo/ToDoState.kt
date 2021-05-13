package kweb.demos.todo

import kotlinx.serialization.Serializable
import kweb.shoebox.Shoebox
import kweb.shoebox.stores.DirectoryStore
import java.nio.file.Files
import java.nio.file.Path

/**
 * Stores all persistent state for this app in a directory, creating it if necessary
 */

class ToDoState(dir: Path) {
    init {
        if (Files.notExists(dir)) {
            Files.createDirectory(dir)
        }
    }

    @Serializable data class List(val uid: String, val title: String)

    @Serializable data class Item(val uid: String, val created : Long, val listUid: String, val text: String)

    val lists = Shoebox(DirectoryStore(dir.resolve("lists"), List.serializer()))

    val items = Shoebox(DirectoryStore(dir.resolve("items"), Item.serializer()))

    private val itemsByList = items.view("itemsByList", Item::listUid)

    fun itemsByList(listUid: String) = itemsByList.orderedSet(listUid, compareBy(Item::created))
}
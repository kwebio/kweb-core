package kweb.demos.todo

import kweb.shoebox.Shoebox
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

/**
 * Stores all persistent state for this app in a directory, creating it if necessary
 */

class ToDoState(dir: Path) {
    init {
        if (Files.notExists(dir)) {
            Files.createDirectory(dir)
        }
    }

    data class List(val uid: String, val title: String)

    data class Item(val uid: String, val created: Instant, val listUid: String, val text: String)

    val lists = Shoebox<List>(dir.resolve("lists"))

    val items = Shoebox<Item>(dir.resolve("items"))

    private val itemsByList = items.view("itemsByList", Item::listUid)

    fun itemsByList(listUid: String) = itemsByList.orderedSet(listUid, compareBy(Item::created))
}
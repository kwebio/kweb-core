package io.kweb.demos.todo

import io.kweb.shoebox.Shoebox
import java.nio.file.*
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

    val itemsByList = items.view("itemsByList", Item::listUid)

    fun itemsByList(listUid: String) = itemsByList.orderedSet(listUid, compareBy(Item::created))
}
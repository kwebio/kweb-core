package io.kweb.demos.todo

import com.github.sanity.shoebox.Shoebox
import java.time.Instant

object State {
    data class List(val uid: String, val title: String)

    data class Item(val uid: String, val created: Instant, val listUid: String, val text: String)

    val lists = Shoebox<List>()

    val items = Shoebox<Item>()

    fun itemsByList(listUid: String) = items.view("itemsByList", Item::listUid).orderedSet(listUid, compareBy(Item::created))
}
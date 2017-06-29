package io.kweb.state.persistent

import com.github.sanity.shoebox.KeyValue
import com.github.sanity.shoebox.OrderedViewSet
import com.github.sanity.shoebox.Shoebox
import io.kweb.Kweb
import io.kweb.dom.element.Element
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.div
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new
import io.kweb.state.Watchable
import java.util.*

/**
 * Created by ian on 6/18/17.
 */

fun <T : Any> ElementCreator<*>.watch(shoebox: Shoebox<T>, key: String): Watchable<T> {
    val value = shoebox[key] ?: throw RuntimeException("Key $key not found")
    val w = Watchable(value)
    w.addListener { _, n -> shoebox[key] = n }
    val changeHandle = shoebox.onChange(key) { _, n, _ -> w.value = n }
    w.onClose { shoebox.deleteChangeListener(key, changeHandle) }
    this.onCleanup(true) {
        w.close()
    }
    return w
}

private data class ItemInfo<ITEM : Any>(val creator : ElementCreator<Element>, val watchable : Watchable<ITEM>)

/**
 *
 *
 * @sample ordered_view_set_sample
 */
fun <ITEM : Any> ElementCreator<*>.watch(orderedViewSet: OrderedViewSet<ITEM>, renderer : ElementCreator<Element>.(Watchable<ITEM>) -> Unit) {
    val items = ArrayList<ItemInfo<ITEM>>()
    for (keyValue in orderedViewSet.keyValueEntries) {
        items += newItem(this, orderedViewSet, keyValue, renderer, insertAtPosition = null)
    }

    val onInsertHandler = orderedViewSet.onInsert{ index, inserted ->
        items.add(index, newItem(this, orderedViewSet, inserted, renderer, index))
    }
    this.onCleanup(true) { orderedViewSet.deleteInsertListener(onInsertHandler) }

    val onRemoveHandler = orderedViewSet.onRemove { index, removedValue ->
        val removed = items.removeAt(index)
        removed.creator.cleanup()
        removed.watchable.close()
    }

    this.onCleanup(true) { orderedViewSet.deleteRemoveListener(onRemoveHandler) }
}

private fun <ITEM : Any> newItem(
        parentEC : ElementCreator<*>,
        orderedViewSet: OrderedViewSet<ITEM>,
        keyValue : KeyValue<ITEM>,
        renderer : ElementCreator<Element>.(Watchable<ITEM>) -> Unit,
        insertAtPosition: Int?)
        : ItemInfo<ITEM> {
    val itemElementCreator = ElementCreator(parentEC.addToElement, parentEC, insertAtPosition)
    val itemWatchable = itemElementCreator.watch(orderedViewSet.view.viewOf, keyValue.key)
    renderer.invoke(itemElementCreator, itemWatchable)
    if (itemElementCreator.elementsCreatedCount != 1) {
        /*
         * Only one element may be created per-item because otherwise it would be much more complicated to figure
         * out where new items should be inserted by the onInsert handler below.  onRemove would be easier because
         * we could just call itemElementCreator.cleanup() to delete that item's elements.
         *
         * This shouldn't be an onerous requirement because typically with lists of things there is just one
          * root <ol> or <ul> per item.  If it does turn out to be a problem we'll need to find another approach.
         */
        throw RuntimeException("""
            Only one element may be created per item but ${itemElementCreator.elementsCreatedCount} were created for
            item key ${keyValue.key}.  Note that this element may have as many children as you like, so you may just need
            to wrap the elements in a <DIV> or other element type.
""".trimIndent())
    }
    return ItemInfo(itemElementCreator, itemWatchable)
}

fun ordered_view_set_sample() {
    data class Cat(val name : String, val color : String)
    val cats = Shoebox<Cat>()
    val catColorView = cats.view("catColors", Cat::color)
    Kweb(port = 1234) {
        doc.body.new {
            watch(catColorView.orderedSet("brown")) {
                div().new {
                    h1().text(it.map(Cat::name))
                }
            }
        }
    }
}
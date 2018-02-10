package io.kweb.state.persistent

import com.github.sanity.shoebox.*
import io.kweb.Kweb
import io.kweb.dom.element.*
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.state.*
import java.util.*

/**
 * Created by ian on 6/18/17.
 */

fun <T : Any> ElementCreator<*>.render(bindable : ReadOnlyBindable<T>, renderer : ElementCreator<Element>.(T) -> Unit) {
    var childEC = ElementCreator(this.addToElement, this)
    bindable.addListener { _, newValue ->
        childEC.cleanup()
        childEC = ElementCreator(this.addToElement, this)
        renderer(childEC, newValue)
    }
    renderer(childEC, bindable.value)
}

fun <T : Any> ElementCreator<*>.asBindable(shoebox: Shoebox<T>, key: String): Bindable<T> {
    val value = shoebox[key] ?: throw RuntimeException("Key $key not found")
    val w = Bindable(value)
    w.addListener { _, n -> shoebox[key] = n }
    val changeHandle = shoebox.onChange(key) { _, n, _ -> w.value = n }
    w.onClose { shoebox.deleteChangeListener(key, changeHandle) }
    this.onCleanup(true) {
        w.close()
    }
    return w
}

private data class ItemInfo<ITEM : Any>(val creator : ElementCreator<Element>, val bindable: Bindable<ITEM>)

/**
 *
 *
 * @sample ordered_view_set_sample
 */
fun <ITEM : Any, EL : Element> ElementCreator<EL>.renderEach(orderedViewSet: OrderedViewSet<ITEM>, renderer : ElementCreator<EL>.(Bindable<ITEM>) -> Unit) {
    val items = ArrayList<ItemInfo<ITEM>>()
    for (keyValue in orderedViewSet.keyValueEntries) {
        items += createItem(orderedViewSet, keyValue, renderer, insertAtPosition = null)
    }

    val onInsertHandler = orderedViewSet.onInsert{ index, inserted ->
        items.add(index, createItem( orderedViewSet, inserted, renderer, index))
    }
    this.onCleanup(true) { orderedViewSet.deleteInsertListener(onInsertHandler) }

    val onRemoveHandler = orderedViewSet.onRemove { index, _ ->
        val removed = items.removeAt(index)
        removed.creator.cleanup()
        removed.bindable.close()
    }

    this.onCleanup(true) { orderedViewSet.deleteRemoveListener(onRemoveHandler) }
}

private fun <ITEM : Any, EL : Element> ElementCreator<EL>.createItem(
        orderedViewSet: OrderedViewSet<ITEM>,
        keyValue : KeyValue<ITEM>,
        renderer : ElementCreator<EL>.(Bindable<ITEM>) -> Unit,
        insertAtPosition: Int?)
        : ItemInfo<ITEM> {
    val itemElementCreator = ElementCreator(this.addToElement, this, insertAtPosition)
    val bindableItem = itemElementCreator.asBindable(orderedViewSet.view.viewOf, keyValue.key)
    renderer(itemElementCreator, bindableItem)
    if (itemElementCreator.elementsCreated != 1) {
        /*
         * Only one element may be created per-item because otherwise it would be much more complicated to figure
         * out where new items should be inserted by the onInsert handler below.  onRemove would be easier because
         * we could just call itemElementCreator.cleanup() to delete that item's elements.
         *
         * This shouldn't be an onerous requirement because typically with lists of things there is just one
          * root <ol> or <ul> per item.  If it does turn out to be a problem we'll need to find another approach.
         */
        throw RuntimeException("""
            Only one element may be created per item but ${itemElementCreator.elementsCreated} were created for
            item key ${keyValue.key}.  Note that this element may have as many children as you like, so you may just need
            to wrap the elements in a <DIV> or other element type.
""".trimIndent())
    }
    return ItemInfo(itemElementCreator, bindableItem)
}

fun ordered_view_set_sample() {
    data class Cat(val name : String, val color : String)
    val cats = Shoebox<Cat>()
    val catColorView = cats.view("catColors", Cat::color)
    Kweb(port = 1234) {
        doc.body.new {
            renderEach(catColorView.orderedSet("brown")) { brownCat ->
                div().new {
                    h1().text(brownCat.map(Cat::name))
                }
            }
        }
    }
}
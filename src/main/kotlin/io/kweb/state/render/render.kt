package io.kweb.state.render

import io.kweb.Kweb
import io.kweb.dom.element.*
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.shoebox.*
import io.kweb.state.*
import mu.KotlinLogging
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by ian on 6/18/17.
 */

private val logger = KotlinLogging.logger {}

fun <T : Any?> ElementCreator<*>.render(kval: KVal<T>, block: ElementCreator<Element>.(T) -> Unit) {

    val containerSpan = span()

    val previousElementCreator : AtomicReference<ElementCreator<Element>?> = AtomicReference(null)

    val listenerHandle = kval.addListener { _, newVal ->
        containerSpan.removeChildren()
        containerSpan.new {
            block(newVal)
            // Remember this ElementCreator and clean up the previous one if necessary
            previousElementCreator.getAndSet(this)?.cleanup()
        }
    }

    containerSpan.new {
        block(kval.value)
    }

    this.onCleanup(true) {
        kval.removeListener(listenerHandle)
    }
}


fun <T : Any> ElementCreator<*>.toVar(shoebox: Shoebox<T>, key: String): KVar<T> {
    val value = shoebox[key] ?: throw NoSuchElementException("Key $key not found")
    val w = KVar(value)
    w.addListener { _, n ->
        require(this.browser.kweb.isNotCatchingOutbound()) {
            """You appear to be modifying Shoebox state from within an onImmediate callback, which
                |should only make simple modifications to the DOM.""".trimMargin()
        }
        shoebox[key] = n
    }
    val changeHandle = shoebox.onChange(key) { _, n, _ -> w.value = n }
    w.onClose { shoebox.deleteChangeListener(key, changeHandle) }
    this.onCleanup(withParent = true) {
        w.close()
    }
    return w
}

private data class ItemInfo<ITEM : Any>(val creator: ElementCreator<Element>, val KVar: KVar<ITEM>)

data class IndexedItem<I>(val index: Int, val total: Int, val item: I)

/**
 *
 *
 * @sample ordered_view_set_sample
 */
fun <ITEM : Any, EL : Element> ElementCreator<EL>.renderEach(orderedViewSet: OrderedViewSet<ITEM>, renderer: ElementCreator<EL>.(KVar<ITEM>) -> Unit) {
    val items = CopyOnWriteArrayList<ItemInfo<ITEM>>()
    for (keyValue in orderedViewSet.keyValueEntries) {
        items += createItem(orderedViewSet, keyValue, renderer, insertAtPosition = null)
    }

    val onInsertHandler = orderedViewSet.onInsert { index, inserted ->
        items.add(index, createItem(orderedViewSet, inserted, renderer, index))
    }
    this.onCleanup(true) { orderedViewSet.deleteInsertListener(onInsertHandler) }

    val onRemoveHandler = orderedViewSet.onRemove { index, keyValue ->
        if (index >= items.size) {
            logger.warn("Invalid index $index to retrieveJs item from items list of size ${items.size} for key ${keyValue.key} and item ${keyValue.value}", RuntimeException())
        } else {
            val removed = items.removeAt(index)
            removed.creator.cleanup()
            removed.KVar.close()
        }
    }

    this.onCleanup(true) {

        orderedViewSet.deleteRemoveListener(onRemoveHandler)
    }
}

private fun <ITEM : Any, EL : Element> ElementCreator<EL>.createItem(
        orderedViewSet: OrderedViewSet<ITEM>,
        keyValue: KeyValue<ITEM>,
        renderer: ElementCreator<EL>.(KVar<ITEM>) -> Unit,
        insertAtPosition: Int?)
        : ItemInfo<ITEM> {
    val itemElementCreator = ElementCreator(this.parent, this, insertAtPosition)
    val itemVar = itemElementCreator.toVar(orderedViewSet.view.viewOf, keyValue.key)
    try {
        renderer(itemElementCreator, itemVar)
    } catch (e: Exception) {
        logger.error("Error rendering item", e)
    }

    if (itemElementCreator.elementsCreatedCount > 1) {
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
    return ItemInfo(itemElementCreator, itemVar)
}

fun ordered_view_set_sample() {
    data class Cat(val name: String, val color: String)

    val cats = Shoebox<Cat>()
    val catColorView = cats.view("catColors", Cat::color)
    Kweb(port = 1234, buildPage = {
        doc.body.new {
            renderEach(catColorView.orderedSet("brown")) { brownCat ->
                div().new {
                    h1().text(brownCat.map(Cat::name))
                }
            }
        }
    })
}


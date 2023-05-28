package kweb.state

import kotlinx.serialization.json.JsonPrimitive
import kweb.Element
import kweb.ElementCreator
import kweb.span


/**
 * Similar to [render], but renders a list of items, and updates the DOM when the list changes.
 *
 * While [render] could be used for this with a `KVal<List<String>>`, this method is far more efficient
 * because it only updates the DOM when the list changes, rather than re-rendering the entire list every time.
 */
fun <ITEM : Any, EL : Element> ElementCreator<EL>.renderEach(
    observableList: ObservableList<ITEM>,
    itemRenderer: ElementCreator<Element>.(ITEM) -> Unit
) {

    //a wrapper made of 2 empty spans around the list of items that renderEach will draw
    //This is needed to be able to use insertBefore on the end span to add items to the end of the list.
    val listFragment = RenderFragment(
        span().classes(RenderSpanNames.listStartMarkerClassName).id,
        span().classes(RenderSpanNames.listEndMarkerClassName).id
    )

    fun insertItem(position: Int, newItem: ITEM,
                   renderHandles: ArrayList<RenderHandle<ITEM>>) {
        val nextElementRenderMarkerStartId: String = if (position == renderHandles.size) {
            listFragment.endId
        } else {
            renderHandles[position].renderFragment.startId
        }
        val itemElementCreator =
            ElementCreator<Element>(this.element, this, nextElementRenderMarkerStartId)
        val kvar = KVar(newItem)
        val newFragment = itemElementCreator.render(kvar) { item ->
            itemRenderer(item)
        }
        renderHandles.add(position, RenderHandle(newFragment, kvar))
    }

    fun <ITEM : Any> deleteItem(position: Int, renderHandles: ArrayList<RenderHandle<ITEM>>) {
        val renderHandleToRemove = renderHandles[position].renderFragment
        renderHandles.removeAt(position)
        browser.callJsFunction("""
var start_id = {};
var end_id = {};
var start_element = document.getElementById(start_id);
var end_element = document.getElementById(end_id);
var parent = start_element.parentNode;
while (start_element.nextSibling != end_element) {
    parent.removeChild(start_element.nextSibling);
}
parent.removeChild(start_element);
parent.removeChild(end_element);
            """, JsonPrimitive(renderHandleToRemove.startId),
            JsonPrimitive(renderHandleToRemove.endId)
        )
        renderHandleToRemove.delete()
    }

    fun moveItemClientSide(itemStartMarker : String, itemEndMarker : String, newPosMarker : String) {
        //This JavaScript takes all elements from one start span to another, denoted by startMarker and endMarker,
        //and inserts them before the element that's ID is passed to the 'newPos' variable.
        //language=JavaScript
        val moveItemCode = """var startMarker = document.getElementById({});
var endMarker = document.getElementById({});
var elementsToMove = [];
var currentElement = startMarker.nextSibling;
while(currentElement !== endMarker) {
    elementsToMove.push(currentElement);
    currentElement = currentElement.nextSibling;
}
var newPos = document.getElementById({});
var listParent = startMarker.parentNode;
listParent.insertBefore(startMarker, newPos);
listParent.insertBefore(endMarker, newPos);
elementsToMove.forEach(function (item){
    listParent.insertBefore(item, endMarker);
});"""
        browser.callJsFunction(moveItemCode, JsonPrimitive(itemStartMarker),
            JsonPrimitive(itemEndMarker), JsonPrimitive(newPosMarker)
        )
    }

    ElementCreator<Element>(this.element, this.parentCreator, insertBefore = listFragment.endId).apply {

        //These renderFragments must be kept in sync with the items in observableList that they're rendering
        val renderHandles = ArrayList<RenderHandle<ITEM>>()

        synchronized(renderHandles) {
            //render the initial observableList to the DOM storing the Handles in renderHandles
            for (item in observableList.getItems()) {
                val kvar = KVar(item)
                val fragment = render(kvar) { fragItem ->
                    itemRenderer(fragItem)
                }
                renderHandles += RenderHandle(fragment, kvar)
            }
        }

        val handle = observableList.addListener { changes ->
            synchronized(renderHandles) {
                // TODO: Consider replacing change in changes with, "mods in modifications", to remove confusion between change, and Modification.Change
                for (change in changes) {
                    // Apply change to DOM using renderHandles, and update renderHandles to keep it in sync with observableList
                    when (change) {
                        is ObservableList.Modification.Change -> {
                            renderHandles[change.position].kvar.value = KVar(change.newItem).value
                        }
                        is ObservableList.Modification.Deletion -> {
                            deleteItem(change.position, renderHandles)
                        }
                        is ObservableList.Modification.Insertion -> {
                            insertItem(change.position, change.item, renderHandles)
                        }
                        is ObservableList.Modification.Move -> {
                            if (change.oldPosition == change.newPosition) {
                                continue
                            }
                            if (change.oldPosition > change.newPosition) {
                                moveItemClientSide(renderHandles[change.oldPosition].renderFragment.startId,
                                    renderHandles[change.oldPosition].renderFragment.endId,
                                    renderHandles[change.newPosition].renderFragment.startId)
                                renderHandles.add(change.newPosition, RenderHandle(renderHandles[change.oldPosition].renderFragment, renderHandles[change.oldPosition].kvar))
                                renderHandles.removeAt(change.oldPosition+1)
                            }
                            else { //change.newPosition > change.oldPosition
                                val newRenderHandle = RenderHandle(renderHandles[change.oldPosition].renderFragment, renderHandles[change.oldPosition].kvar)
                                val startId = if (change.newPosition == renderHandles.size-1) {
                                    listFragment.endId
                                } else {
                                    renderHandles[change.newPosition+1].renderFragment.startId
                                }
                                moveItemClientSide(renderHandles[change.oldPosition].renderFragment.startId,
                                    renderHandles[change.oldPosition].renderFragment.endId,
                                    startId)
                                if (change.newPosition == renderHandles.size-1) {
                                    renderHandles.add(newRenderHandle)
                                } else {
                                    renderHandles.add(change.newPosition+1, newRenderHandle)
                                }
                                renderHandles.removeAt(change.oldPosition)
                            }
                        }
                    }
                }
            }
        }

        onCleanup(true) {
            observableList.removeListener(handle)
        }
    }
}


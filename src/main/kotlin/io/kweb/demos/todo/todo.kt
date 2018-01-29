package io.kweb.demos.todo

import io.kweb.*
import io.kweb.demos.todo.Route.TodoPath
import io.kweb.demos.todo.Route.TodoPath.*
import io.kweb.demos.todo.State.Item
import io.kweb.demos.todo.State.List
import io.kweb.dom.element.*
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.creation.tags.InputType.text
import io.kweb.dom.element.events.on
import io.kweb.plugins.semanticUI.*
import io.kweb.routing.route
import io.kweb.state.Bindable
import io.kweb.state.persistent.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.future.await
import java.time.Instant

fun main(args: Array<String>) {
    // Starts a web server listening on port 8091
    Kweb(port = 8091, plugins = listOf(semanticUIPlugin)) {
        doc.body.new {
            route<TodoPath> {
                render(path) { thisPath ->
                    when (thisPath) {
                        is Root -> {
                            val newListId = generateNewUid()
                            State.lists[newListId] = State.List(newListId, "")
                            path.value = Lists(newListId)
                        }
                        is Lists -> {
                            bind(asBindable(State.lists, thisPath.uid))
                        }
                    }
                }
            }
        }
    }
    Thread.sleep(10000)
}

private fun ElementCreator<*>.bind(list : Bindable<State.List>) {
    h3().text(list.map(List::title))
    div(semantic.ui.middle.aligned.divided.list).new {
        renderEach(State.itemsByList(list.value.uid)) { item ->
            div(semantic.item).new {
                div(semantic.right.floated.content).new {
                    bindRemoveButton(item)
                }
                div(semantic.content).text(item.map(Item::text))
            }
        }
    }
    div(semantic.ui.action.input).new {
        val input = input(text, placeholder = "Add Item")
        input.on.keypress { ke ->
            if (ke.code == "13") {
                handleAddItem(input, list)
            }
        }
        button(semantic.ui.button).text("Add").on.click {
            handleAddItem(input, list)
        }
    }
}

private fun handleAddItem(input: InputElement, list: Bindable<List>) {
    async {
        val newItemText = input.read.text.await()
        input.text("")
        val newItem = Item(generateNewUid(), Instant.now(), list.value.uid, newItemText)
        State.items[newItem.uid] = newItem
    }
}

private fun ElementCreator<DivElement>.bindRemoveButton(item: Bindable<Item>): Element {
    return div(semantic.ui.button).text("Remove").on.click {
        State.items.remove(item.value.uid)
    }
}

private fun generateNewUid() = random.nextInt(100_000_000).toString(16)

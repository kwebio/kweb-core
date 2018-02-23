package io.kweb.demos.todo

import io.kweb.*
import io.kweb.demos.todo.State.Item
import io.kweb.demos.todo.State.List
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.creation.tags.InputType.text
import io.kweb.dom.element.events.on
import io.kweb.dom.element.new
import io.kweb.plugins.semanticUI.*
import io.kweb.routing.*
import io.kweb.state.Bindable
import io.kweb.state.persistent.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.future.await
import mu.KotlinLogging
import java.time.Instant

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    // Starts a web server listening on port 8091
    Kweb(port = 8093, debug = true, plugins = listOf(semanticUIPlugin)) {
        doc.body.new {
            div(Style.outerContainer).new {
                div(Style.innerContainer).new {
                    route(withGalimatiasUrlParser) { url ->
                        val listHeadingStyle = semantic.ui.dividing.header
                        h1(listHeadingStyle).text("Shopping list")
                        div(semantic.content).new {
                            render(url.path[0]) { entityType ->
                                logger.info("Rendering entity type $entityType")
                                when (entityType) {
                                    ROOT_PATH -> {
                                        createNewListAndRedirect(url.path)
                                    }
                                    "lists" -> {
                                        logger.info("Rendering lists/${url.path[1]}")
                                        render(url.path[1]) { listUid ->
                                            try {
                                                val list = asBindable(State.lists, listUid)
                                                renderList(list)
                                            } catch (e : NoSuchElementException) {
                                                throw NotFoundException("Can't find list with id $listUid")
                                            }
                                        }
                                    }
                                    else -> {
                                        throw NotFoundException("Unrecognized entity type '$entityType', path: ${url.path.value}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    Thread.sleep(10000)
}

private fun createNewListAndRedirect(path: Bindable<kotlin.collections.List<String>>) {
    val newListId = generateNewUid()
    State.lists[newListId] = List(newListId, "")
    logger.info("Redirecting from root to lists/$newListId")
    path.value = listOf("lists", newListId)
}

private fun ElementCreator<*>.renderList(list: Bindable<State.List>) {
    logger.info("Rendering list ${list.value.uid}")
    h3().text(list.map(List::title))
    div(semantic.ui.middle.aligned.divided.list).new {
        renderEach(State.itemsByList(list.value.uid)) { item ->
            logger.info("Rendering list item ${item.value.uid}")
            div(semantic.item).new {
                div(semantic.right.floated.content).new {
                    renderRemoveButton(item)
                }
                div(semantic.content).text(item.map(Item::text))
            }
        }
    }
    logger.info("Rendering Add Item button")
    div(semantic.ui.action.input).new {
        val input = input(text, placeholder = "Add Item")
        input.on.keypress { ke ->
            if (ke.code == "Enter") {
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
        val newItemText = input.getValue().await()
        input.setValue("")
        val newItem = Item(generateNewUid(), Instant.now(), list.value.uid, newItemText)
        State.items[newItem.uid] = newItem
    }
}

private fun ElementCreator<DivElement>.renderRemoveButton(item: Bindable<Item>) {
    val button = button(semantic.mini.ui.icon.button)
    button.new {
        i(semantic.trash.icon)
    }
    button.on.click {
        State.items.remove(item.value.uid)
    }
}

private fun generateNewUid() = random.nextInt(100_000_000).toString(16)

package io.kweb.demos.todo

import io.kweb.*
import io.kweb.dom.BodyElement
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.creation.tags.InputType.text
import io.kweb.dom.element.events.*
import io.kweb.dom.element.new
import io.kweb.plugins.semanticUI.semanticUIPlugin
import io.kweb.routing.*
import io.kweb.state.KVar
import io.kweb.state.persistent.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.future.await
import mu.KotlinLogging
import java.time.Instant
import io.kweb.plugins.semanticUI.semantic as s

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    // Starts a web server listening on port 8091
    Kweb(port = 8091, debug = true, plugins = listOf(semanticUIPlugin)) {
        doc.body.new {

            pageBorderAndTitle("Todo List") {

                val url = doc.receiver.url(simpleUrlParser)

                div(s.content).new {
                    render(url.path[0]) { entityType ->
                        when (entityType) {
                            ROOT_PATH -> {
                                val newListId = createNewList()
                                url.path.value = listOf("lists", newListId)
                            }
                            "lists" -> {
                                render(url.path[1]) { listId ->
                                    try {
                                        render(toVar(State.lists, listId))
                                    } catch (e: NoSuchElementException) {
                                        throw NotFoundException("Can't find list with id $listId")
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
    Thread.sleep(10000)
}

private fun ElementCreator<BodyElement>.pageBorderAndTitle(title: String, content: ElementCreator<DivElement>.() -> Unit) {
    div(s.ui.three.column.centered.grid).new {
        div(s.column).new {
            h1(s.ui.dividing.header).text(title)
            content(this)
        }
    }
}

private fun createNewList(): String {
    val newListId = generateNewUid()
    State.lists[newListId] = State.List(newListId, "")
    return newListId
}

private fun ElementCreator<*>.render(list: KVar<State.List>) {
    h3().text(list.map(State.List::title))
    div(s.ui.middle.aligned.divided.list).new {
        renderEach(State.itemsByList(list.value.uid)) { item ->
            div(s.item).new {
                div(s.right.floated.content).new {
                    renderRemoveButton(item)
                }
                div(s.content).text(item.map(State.Item::text))
            }
        }
    }
    logger.info("Rendering Add Item button")
    div(s.ui.action.input).new {
        val input = input(text, placeholder = "Add Item")
        input.on.keypress { ke ->
            if (ke.code == "Enter") {
                handleAddItem(input, list)
            }
        }
        button(s.ui.button).text("Add").apply {
            onImmediate.click {
                execute("console.info(\"immediate\");")
            }
            on.click {
                handleAddItem(input, list)
                execute("console.info(\"after\");")
            }
        }
    }
}

private fun handleAddItem(input: InputElement, list: KVar<State.List>) {
    async {
        val newItemText = input.getValue().await()
        input.setValue("")
        val newItem = State.Item(generateNewUid(), Instant.now(), list.value.uid, newItemText)
        State.items[newItem.uid] = newItem
    }
}

private fun ElementCreator<DivElement>.renderRemoveButton(item: KVar<State.Item>) {
    val button = button(s.mini.ui.icon.button)
    button.new {
        i(s.trash.icon)
    }
    button.on.click {
        State.items.remove(item.value.uid)
    }
}

private fun generateNewUid() = random.nextInt(100_000_000).toString(16)

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
import io.mola.galimatias.URL
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.future.await
import mu.KotlinLogging
import java.time.Instant
import io.kweb.plugins.semanticUI.semantic as s

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    /** A simple yet flexible plugin mechanism */
    val plugins = listOf(semanticUIPlugin)

    /** Create a Kweb instance, and configure it to use the Semantic
     * UI framework. Build a simple to-do list app listening on
     * http://localhost:8091/
     * */
    Kweb(port = 8091, debug = true, plugins = plugins) {
        doc.body.new {

            /** Kweb allows you to modularize your code however suits your needs
                best.  Here I use an extension function to renderList some common
                outer page elements */
            pageBorderAndTitle("Todo List") {

                /** A KVar is similar to an AtomicReference in the standard Java
                    Library, but which supports the observer pattern and `map`
                    semantics.  Here we set it to the current URL of the page.  This
                    will update automatically the page's URL changes, and can be
                    modified to update the page's URL,
                    as you'll see below. */
                val url: KVar<URL> = doc.receiver.url(simpleUrlParser)

                /** s.content uses the semanticUIPlugin to use the excellent
                    Semantic UI framework, included as a plugin above, and implemented
                    as a convenient DSL within Kweb */
                div(s.content).new {

                    /** Note how url.path[0] is itself a KVar.  Changes to firstPathElement
                        will automatically propagate _bi-directionally_ with `url`.  This
                        comes in very handy later. */
                    val firstPathElement: KVar<String> = url.path[0]

                    /** Renders `firstPathElement`, but - and here's the fun part - will
                        automatically re-renderList if firstPathElement changes.  This is
                        a simple, elegant, and yet powerful routing mechanism. */
                    render(firstPathElement) { entityType ->
                        when (entityType) {
                            ROOT_PATH -> {
                                val newListId = createNewList()
                                url.path.value = listOf("lists", newListId)
                            }
                            "lists" -> {
                                /** Renders can be nested, which means that only this
                                    specific part of the page must be re-rendered if
                                    url.path[1] changes, which is both incredibly convenient
                                    for the developer, and efficient, leading to a very
                                    responsive webapp. */
                                render(url.path[1]) { listId ->
                                    try {
                                        /** Here we use the same render mechanism to tie DOM
                                            state to persistent state stored in Shoebox,
                                            Kweb's simple but powerful key-value store with
                                            observer pattern support.  */
                                        renderList(toVar(State.lists, listId))
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

private fun ElementCreator<*>.renderList(list: KVar<State.List>) {
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

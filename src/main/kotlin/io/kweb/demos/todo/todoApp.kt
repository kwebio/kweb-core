package io.kweb.demos.todo

import io.kweb.*
import io.kweb.dom.BodyElement
import io.kweb.dom.element.creation.ElementCreator
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.creation.tags.InputType.text
import io.kweb.dom.element.events.*
import io.kweb.dom.element.new
import io.kweb.plugins.semanticUI.*
import io.kweb.routing.*
import io.kweb.shoebox.Shoebox
import io.kweb.state.KVar
import io.kweb.state.persistent.*
import io.mola.galimatias.URL
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import mu.KotlinLogging
import java.time.Instant
import java.util.*

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
                best.  Here I use an extension function defined elsewhere to
                draw some common outer page DOM elements */

            pageBorderAndTitle("Todo List") {

                /** A KVar is similar to an AtomicReference in the standard Java
                    Library, but which supports the observer pattern and `map`
                    semantics.  Here I set it to the current URL of the page.

                    This will update automatically if the page'semantic URL changes, and
                    if it is modified, the page'semantic URL will change and the DOM will
                    re-render _without_ a page reload.  Yes, seriously. */
                val url: KVar<URL> = doc.receiver.url(simpleUrlParser)

                val path: KVar<List<String>> = url.path

                path.value = listOf("users", "12345")

                /** semantic.content uses the semanticUIPlugin to use the excellent
                    Semantic UI framework, included as a plugin above, and implemented
                    as a convenient DSL within Kweb */
                div(semantic.content).new {

                    /** Note how url.path[0] is itself a KVar.  Changes to firstPathElement
                        will automatically propagate _bi-directionally_ with `url`.  This
                        comes in very handy later. */
                    val firstPathElement: KVar<String> = url.path[0]

                    /** Renders `firstPathElement`, but - and here'semantic the fun part - will
                        automatically re-render if firstPathElement changes.  This is
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
                                    url.path[1] changes, which is very convenient
                                    for the developer in comparison to other frameworks,
                                    while minimizing server-browser chatter. */
                                render(url.path[1]) { listId ->
                                    try {
                                        /** Here I use the same render mechanism to tie DOM
                                            state to persistent state stored in Shoebox,
                                            Kweb'semantic simple but powerful key-value store with
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
    div(semantic.ui.three.column.centered.grid).new {
        div(semantic.column).new {
            h1(semantic.ui.dividing.header).text(title)
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
    div(semantic.ui.middle.aligned.divided.list).new {
        renderEach(State.itemsByList(list.value.uid)) { item ->
            div(semantic.item).new {
                div(semantic.right.floated.content).new {
                    renderRemoveButton(item)
                }
                div(semantic.content).text(item.map(State.Item::text))
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
        button(semantic.ui.button).text("Add").apply {
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
    GlobalScope.launch {
        val newItemText = input.getValue().await()
        input.setValue("")
        val newItem = State.Item(generateNewUid(), Instant.now(), list.value.uid, newItemText)
        State.items[newItem.uid] = newItem
    }
}

private fun ElementCreator<DivElement>.renderRemoveButton(item: KVar<State.Item>) {
    val button = button(semantic.mini.ui.icon.button)
    button.new {
        i(semantic.trash.icon)
    }
    button.on.click {
        State.items.remove(item.value.uid)
    }
}

private fun generateNewUid() = random.nextInt(100_000_000).toString(16)

fun temp() {
    data class User(val name : String, val email : String)
    val users = Shoebox<User>()
    users["aaa"] = User("Ian", "ian@ian.ian")

    Kweb(port = 1234) {
        doc.body.new {
            val user = toVar(users, "aaa")
            ul().new {
                li().text(user.map {"Name: ${it.name}"})
                li().text(user.map {"Email: ${it.email}"})

            }
        }
    }
}

fun temp2() {
    Kweb(port = 1234) {
        doc.body.new {
            val url: KVar<URL> = url(simpleUrlParser)
            render(url.path) { path ->
                ul().new {
                    for (p in path) {
                        li().text(p)
                    }
                }
            }
        }
    }
}

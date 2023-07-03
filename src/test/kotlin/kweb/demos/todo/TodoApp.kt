package kweb.demos.todo
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.state.ObservableList
import kweb.state.render
import kweb.state.renderEach
import kweb.util.NotFoundException
import kweb.util.random
import mu.two.KotlinLogging

fun main() {
    TodoApp()
}

class TodoApp {

    private val logger = KotlinLogging.logger {}

    class TodoList(var title: String, val todoItems: ObservableList<String>)

    private val todoLists = mutableMapOf<String, TodoList>()
    val plugins = listOf(fomanticUIPlugin)
    val server: Kweb

    init {

        /** Create a Kweb instance, and configure it to use the Fomantic
         * UI framework. Build a simple to-do list app listening on
         * http://localhost:7659/
         * */
        server = Kweb(port = 7659, debug = true, plugins = plugins, buildPage = {

            doc.head {
                // Not required, but recommended by HTML spec
                meta(name = "Description", content = "A simple To Do list app to demonstrate Kweb")
            }

            doc.body {
                /** Kweb allows you to modularize your code however suits your needs
                best.  Here I use an extension function defined elsewhere to
                draw some util outer page DOM elements */
                pageBorderAndTitle("To do List") {
                    div(fomantic.content) {
                        route {

                            path("/") {
                                val newListId = createNewList()
                                /**
                                 * This will cause the page to switch to the newly created list automatically, and
                                 * without a page refresh.
                                 */

                                url.value = "/lists/$newListId"
                            }

                            path("/lists/{id}") { params ->
                                doc.head {
                                    title().text("To Do List #${params.getValue("id").value}")
                                }
                                render(params.getValue("id")) { listId ->
                                    logger.info("Rendering list id $listId")

                                    try {
                                        renderList(listId)
                                    } catch (e: NoSuchElementException) {
                                        throw NotFoundException("Can't find list with id $listId")
                                    }
                                }
                            }

                            /*
                                 * It's not necessary, but we can also define a custom 404 handler:
                                 */
                            notFound {
                                div(fomantic.ui.negative.message) {
                                    div(fomantic.header).text("Not Found :(")
                                    p().text(url.map { "Unable to find path $it" })
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun ElementCreator<*>.pageBorderAndTitle(title: String, content: ElementCreator<DivElement>.(DivElement) -> Unit) {
        div(fomantic.ui.main.container) {
            div(fomantic.column) {
                div(fomantic.ui.vertical.segment) {
                    div(fomantic.ui.message) {
                        p().innerHTML(
                            """
A simple demo of <a href="https://docs.kweb.io/">Kweb</a>, add and delete items from a
to do list.
<p/>
Try visiting this URL in another browser window and make some changes.
<p/>
You may find the source code for this app
<a href="https://github.com/kwebio/kweb-core/tree/master/src/main/kotlin/kweb/demos/todo">here</a>.
"""
                                
                        )
                    }
                }

                div(fomantic.ui.vertical.segment) {
                    h1(fomantic.ui.dividing.header).text(title)
                    content(it)
                }
            }
        }
    }

    private fun createNewList(): String {
        val newListId = generateNewUid()
        todoLists[newListId] = TodoList(newListId, ObservableList<String>(mutableListOf()))
        return newListId
    }

    private fun ElementCreator<*>.renderList(activeListKey: String) {
        //This line of code just inserts the List title under To do List on the webpage. I don't think this was intended
        //But, it's a copy of the line of code that used to be here. It doesn't seem to work on Master though
        //The title is an empty Kvar() when run on the master branch.
        h3().text(todoLists[activeListKey]!!.title)

        div(fomantic.ui.middle.aligned.divided.list) {
            renderEach(todoLists[activeListKey]!!.todoItems) { item ->
                div(fomantic.item) {
                    div(fomantic.right.floated.content) {
                        renderRemoveButton(activeListKey, item)
                    }
                    div(fomantic.content) {
                        element.text(item)
                    }
                }
            }
        }
        div(fomantic.ui.action.input) {
            val input = input(type = InputType.text, placeholder = "Add Item")
            // Note that an event can optionally evaluate a javascript expression and retrieve the
            // result, which is supplied to the event handler in event.retrieved.
            input.on(retrieveJs = input.valueJsExpression).keypress { event ->
                if (event.code == "Enter") {
                    handleAddItem(activeListKey, input, event.retrieved.jsonPrimitive.content)
                }
            }
            button(fomantic.ui.button).text("Add").apply {
                on(retrieveJs = input.valueJsExpression).click { event ->
                    handleAddItem(activeListKey, input, event.retrieved.jsonPrimitive.content)
                }
            }
        }

    }

    private fun handleAddItem(activeListKey: String, input: InputElement, newItemText: String) {
        input.setValue("")
        todoLists[activeListKey]!!.todoItems.add(newItemText)
    }

    private fun ElementCreator<DivElement>.renderRemoveButton(activeListKey : String, item: String) {
        val button = button(fomantic.mini.ui.icon.button)
        button.new {
            i(fomantic.trash.icon)
        }
        button.on.click {
            todoLists[activeListKey]!!.todoItems.remove(item)
        }
    }

    private fun generateNewUid() = random.nextInt(100_000_000).toString(16)
}

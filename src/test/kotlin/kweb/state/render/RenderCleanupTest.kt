package kweb.state.render

import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumJupiter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.state.KVar
import kweb.state.ReversibleFunction
import kweb.state.render
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions

@ExtendWith(SeleniumJupiter::class)
class RenderCleanupTest {
    companion object {
        private lateinit var renderCleanupTestApp: RenderCleanupTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            renderCleanupTestApp = RenderCleanupTestApp()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            renderCleanupTestApp.server.close()
        }

        //selenium-jupiter will automatically fall back if the first browser it tries doesn't work
        //https://bonigarcia.github.io/selenium-jupiter/#generic-driver
        @Options
        var chromeOptions = ChromeOptions().apply {
            setHeadless(true)
        }

        @Options
        var firefoxOptions = FirefoxOptions().apply {
            setHeadless(true)
        }
    }
    /*
    @Test
    fun initialRender(driver : WebDriver) {
        driver.get("http://localhost:7659/")
        val h1 = driver.findElement(By.tagName("H1"))
        h1.shouldNotBeNull()
    }
     */
}

fun main() {
    RenderCleanupTestApp()
}

data class TaskList(val tasks: List<String>)

class RenderCleanupTestApp {
    val taskList = KVar(emptyList<String>())

    val server: Kweb = Kweb(port = 7659, plugins = listOf(fomanticUIPlugin)) {

        val editing = KVar(false)

        doc.body {
            render(editing) { _editing ->
                if (_editing) {
                    div(fomantic.ui.form) {
                        div(fomantic.field) {
                            label().text("What tasks would you like to prioritize?  (one per line)")
                            val ta = textArea()
                            ta.setValue(taskList.value.joinToString(separator = "\n"))
                            div(fomantic.ui.buttons) {
                                button(fomantic.ui.button, type = ButtonType.submit).text("Save")
                                    .on("document.getElementById(${ta.id})").click { event ->
                                        //TODO, I'm not sure about this change
                                        taskList.value = Json.decodeFromJsonElement(event.retrieved)
                                        //taskList.value = event.retrieved!!//.split('\n').map { it.trim() }.toList()
                                        editing.value = false
                                    }
                                button(fomantic.ui.button, type = ButtonType.submit).text("Cancel")
                                    .on("document.getElementById(${ta.id}).value").click {
                                        editing.value = false
                                    }
                            }
                        }
                    }
                } else {
                    render(taskList.map { it.size }) { listSize ->
                        div(fomantic.ui.bulleted.list) {
                            for (ix in 0 until listSize) {
                                div(fomantic.item).text(taskList[ix])
                            }
                        }
                        button(fomantic.ui.button).text("Edit").on.click {
                            editing.value = true
                        }
                        Unit
                    }
                }
            }
        }
    }
}

private val stringBool = object : ReversibleFunction<Boolean, String>(label = "bool -> string") {
    override fun invoke(from: Boolean) = if (from) "true" else "false"
    override fun reverse(original: Boolean, change: String) = change == "true"
}

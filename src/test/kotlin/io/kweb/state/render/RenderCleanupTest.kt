package io.kweb.state.render

import io.github.bonigarcia.seljup.*
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.*
import io.kweb.dom.element.creation.tags.InputType.text
import io.kweb.dom.element.events.on
import io.kweb.dom.element.new
import io.kweb.state.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
/*
fun main() {
    RenderTestApp()
    Thread.sleep(1000000)
}

*/
@ExtendWith(SeleniumExtension::class)
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

    @Test
    fun initialRender(driver : WebDriver) {
        driver.get("http://localhost:7659/")
        val h1 = driver.findElement<WebElement>(By.tagName("H1"))
        h1.shouldNotBeNull()
    }
}

fun main() {
    RenderCleanupTestApp()
}

class RenderCleanupTestApp {
    val v = KVar("")

    val server: Kweb = Kweb(port = 7659) {
        doc.body.new {
            button().apply {
                text("Start")
                on.click {
                    v.value = "1"

                    v.value = "2"
                }
            }

            render(v){
                p().text(v.value)
            }
        }
    }

}

private val stringBool = object : ReversableFunction<Boolean, String>(label = "bool -> string") {
    override fun invoke(from: Boolean) = if (from) "true" else "false"
    override fun reverse(original: Boolean, change: String) = change == "true"
}
package kweb.routing

import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumExtension
import kweb.*
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.state.KVar
import kweb.state.ReversibleFunction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions


@ExtendWith(SeleniumExtension::class)
class RoutingTest {
    companion object {
        private lateinit var routingTestApp: RoutingTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            routingTestApp = RoutingTestApp()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            routingTestApp.server.close()
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
        val h1 = driver.findElement<WebElement>(By.tagName("H1"))
        h1.shouldNotBeNull()
    }
     */
}

fun main() {
    RoutingTestApp()
}

class RoutingTestApp {
    val taskList = KVar(emptyList<String>())

    val server: Kweb = Kweb(port = 7659, plugins = listOf(fomanticUIPlugin)) {

        doc.body.new {
            val uri = gurl.path
            route {
                path("/this/has/four/{segments}") {
                    h1().text("This has four segments")
                    a(href = null).text("Go to three segments").on.click { uri.value = "/this/has/three" }
                }
                path("/this/has/three") {
                    h1().text("This has three segments")
                }
            }
        }


    }

}

private val stringBool = object : ReversibleFunction<Boolean, String>(label = "bool -> string") {
    override fun invoke(from: Boolean) = if (from) "true" else "false"
    override fun reverse(original: Boolean, change: String) = change == "true"
}
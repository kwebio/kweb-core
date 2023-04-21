package kweb

import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.kotest.matchers.shouldBe
import kweb.*
import kweb.plugins.FaviconPlugin
import kweb.state.KVar
import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ThreadGuard
import org.openqa.selenium.support.ui.Select

@ExtendWith(SeleniumJupiter::class)
class SelectValueTest {
    companion object {
        private lateinit var selectValueTestApp: SelectValueTestApp

        init {
            System.setProperty("ChromeDr" +
                    "iver.http.factory", "jdk-http-client")
        }

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            selectValueTestApp = SelectValueTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            selectValueTestApp.server.close()
        }

        @Options
        var chromeOptions = ChromeOptions().apply {
            addArguments("--headless=new")
        }

    }

    @Test
    fun mainTest(driver : ChromeDriver) {
        driver.get("http://localhost:7668/")
        val select = Select(driver.findElement(By.tagName("select")))
        Awaitility.await().untilAsserted { selectValueTestApp.selectValue.value shouldBe "" }
        select.selectByValue("cat")
        Awaitility.await().untilAsserted { selectValueTestApp.selectValue.value shouldBe "cat" }
        select.selectByValue("dog")
        Awaitility.await().untilAsserted { selectValueTestApp.selectValue.value shouldBe "dog" }
    }
}

class SelectValueTestApp {

    internal lateinit var selectValue: KVar<String>

    val server: Kweb = Kweb(port = 7668) {
        doc.body {
            val select = select(name = "pets") {
                option().set("value", "dog").text("Dog")
                option().set("value", "cat").text("Cat")
            }
            selectValue = select.value
        }
    }
}

package kweb.diffs

import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumExtension
import io.kotlintest.shouldBe
import kweb.*
import kweb.state.KVar
import kweb.state.render
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions

@ExtendWith(SeleniumExtension::class)
class AppendTextTest {
    companion object {
        private lateinit var appendTestApp: AppendTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            AppendTextTest.appendTestApp = AppendTestApp()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            appendTestApp.server.close()
        }

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
    fun appendTextToMiddle(driver: WebDriver) {
        driver.get("http://localhost:7659/")
        appendTestApp.updateText("Lazy Reddish Brown Fox")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        inputField.getAttribute("value").shouldBe("Lazy Reddish Brown Fox")
    }
}

class AppendTestApp {
    private lateinit var input: InputElement

    val server: Kweb = Kweb(port= 7659) {
        doc.body.new {
            val inputKvar = KVar(false)
            render(inputKvar) {
                input = input(type = InputType.text)
                input.setValue("Lazy Brown Fox")
            }
        }
    }

    fun updateText(newString: String) {
        input.setValue(KVar<String>(newString), "input")
    }
}

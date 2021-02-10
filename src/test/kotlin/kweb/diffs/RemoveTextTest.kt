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
class RemoveTextTest {
    companion object {
        private lateinit var removeTestApp: RemoveTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            RemoveTextTest.removeTestApp = RemoveTestApp()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            removeTestApp.server.close()
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
    fun removeTextFromMiddle(driver: WebDriver) {
        driver.get("http://localhost:7659/")
        removeTestApp.updateText("Lazy Reddish Brown Fox")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        inputField.getAttribute("value").shouldBe("Lazy Reddish Brown Fox")
    }
}

class RemoveTestApp {
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

package kweb

import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumExtension
import io.github.bonigarcia.seljup.WebDriverCreator
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import kweb.state.KVar
import kweb.state.render
import kweb.state.render.RenderTest
import kweb.state.render.RenderTestApp
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.support.ThreadGuard

@ExtendWith(SeleniumExtension::class)
class PreludeTest() {

    companion object {
        private lateinit var preludeTestApp: PreludeTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            PreludeTest.preludeTestApp = PreludeTestApp()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            PreludeTest.preludeTestApp.server.close()
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

    //These tests change the string in the input field, and then make sure that the new setValue() function returns the correct result
    @Test
    fun appendTextFromBeginning(driver: WebDriver) {
        driver.get("http://localhost:7659/")
        preludeTestApp.updateText("Super Lazy Brown Fox")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        Thread.sleep(300)
        inputField.getAttribute("value").shouldBe("Super Lazy Brown Fox")
    }

    @Test
    fun appendTextFromMiddle(driver: WebDriver) {
        driver.get("http://localhost:7659/")
        preludeTestApp.updateText("Lazy Reddish Brown Fox")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        Thread.sleep(300)
        inputField.getAttribute("value").shouldBe("Lazy Reddish Brown Fox")
    }

    @Test
    fun appendTextFromEnd(driver: WebDriver) {
        driver.get("http://localhost:7659/")
        preludeTestApp.updateText("Lazy Brown Fox Jumped")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        Thread.sleep(300)
        inputField.getAttribute("value").shouldBe("Lazy Brown Fox Jumped")
    }

    @Test
    fun removeTextFromBeginning(driver: WebDriver) {
        driver.get("http://localhost:7659/")
        preludeTestApp.updateText("Brown Fox")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        Thread.sleep(300)
        inputField.getAttribute("value").shouldBe("Brown Fox")
    }

    @Test
    fun removeTextFromMiddle(driver: WebDriver) {
        driver.get("http://localhost:7659/")
        preludeTestApp.updateText("Lazy Fox")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        Thread.sleep(300)
        inputField.getAttribute("value").shouldBe("Lazy Fox")
    }

    @Test
    fun removeTextFromEnd(driver: WebDriver) {
        driver.get("http://localhost:7659/")
        preludeTestApp.updateText("Lazy Brown")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        Thread.sleep(300)
        inputField.getAttribute("value").shouldBe("Lazy Brown")
    }
}

class PreludeTestApp {
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
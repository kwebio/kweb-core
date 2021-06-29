package kweb
/*
import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.SeleniumExtension
import io.kotlintest.shouldBe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kweb.*
import kweb.state.KVar
import kweb.state.render
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ThreadGuard

@ExtendWith(SeleniumExtension::class)
class StringDiffTest(@Arguments("--headless") private var driver: WebDriver) {
//class StringDiffTest(private var driver: WebDriver) {

    init {
		//ThreadGuard.protect ensures that the webdriver can only be called by the thread that created it
		//This should make this test thread safe.
        driver = ThreadGuard.protect(driver)
    }

    companion object {
        private lateinit var stringDiffTestApp: StringDiffTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            StringDiffTest.stringDiffTestApp = StringDiffTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            StringDiffTest.stringDiffTestApp.server.close()
        }
    }

    @Test
    fun appendTextToBeginning() {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        inputField.sendKeys(Keys.HOME)
        inputField.sendKeys("Super ")
        inputField.sendKeys(Keys.ENTER)
        stringDiffTestApp.getValue().shouldBe("Super Lazy Brown Fox")
    }

    @Test
    fun appendTextToMiddle() {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        inputField.sendKeys(Keys.HOME)
        for (i in 0 until 5) {
            inputField.sendKeys(Keys.ARROW_RIGHT)
            Thread.sleep(200);
        }
        inputField.sendKeys("Reddish ")
        inputField.sendKeys(Keys.ENTER)
        stringDiffTestApp.getValue().shouldBe("Lazy Reddish Brown Fox")
    }

    @Test
    fun appendTextToEnd() {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        inputField.sendKeys(Keys.END)
        inputField.sendKeys(" Jumped")
        inputField.sendKeys(Keys.ENTER)
        stringDiffTestApp.getValue().shouldBe("Lazy Brown Fox Jumped")
    }

    @Test
    fun removeTextFromBeginning() {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        inputField.sendKeys(Keys.HOME)
        for (i in 0 until 5) {
            inputField.sendKeys(Keys.DELETE)
        }
        inputField.sendKeys(Keys.ENTER)
        stringDiffTestApp.getValue().shouldBe("Brown Fox")
    }

    @Test
    fun removeTextFromMiddle() {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        inputField.sendKeys(Keys.HOME)
        for (i in 0 until 5) {
            inputField.sendKeys(Keys.ARROW_RIGHT)
        }
        for (i in 0 until 6) {
            inputField.sendKeys(Keys.DELETE)
        }
        inputField.sendKeys(Keys.ENTER)
        stringDiffTestApp.getValue().shouldBe("Lazy Fox")
    }

    @Test
    fun removeTextFromEnd() {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement<WebElement>(By.tagName("input"))
        inputField.sendKeys(Keys.END)
        for (i in 0 until 4) {
            inputField.sendKeys(Keys.BACK_SPACE)
        }
        inputField.sendKeys(Keys.ENTER)
        stringDiffTestApp.getValue().shouldBe("Lazy Brown")
    }

}

class StringDiffTestApp {
    private lateinit var input: InputElement

    val server: Kweb = Kweb(port= 7660) {
        doc.body.new {
            val inputKvar = KVar(false)
            render(inputKvar) {
                input = input(type = InputType.text, initialValue = "Lazy Brown Fox")
            }
        }
    }

    suspend fun getValue(): String {
            return input.getValue()
    }
}
*/


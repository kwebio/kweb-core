import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.SeleniumExtension
import io.kotlintest.shouldBe
import kotlinx.coroutines.delay
import kweb.*
import kweb.state.KVar
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ThreadGuard

@ExtendWith(SeleniumExtension::class)
class InputCheckedTest(@Arguments("--headless") private var driver: WebDriver) {

    init {
		//ThreadGuard.protect ensures that the webdriver can only be called by the thread that created it
		//This should make this test thread safe.
        driver = ThreadGuard.protect(driver)
    }

    companion object {
        private lateinit var inputCheckedTestApp: InputCheckedTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            InputCheckedTest.inputCheckedTestApp = InputCheckedTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            InputCheckedTest.inputCheckedTestApp.server.close()
        }
    }

    @Test
    fun checkBeforeAndAfterClick() {
        driver.get("http://localhost:7667/")
        val input = driver.findElement<WebElement>(By.tagName("input"))
        inputCheckedTestApp.checkKVar.value shouldBe false
        input.click()
        Thread.sleep(100)
        inputCheckedTestApp.checkKVar.value shouldBe true
        Thread.sleep(100)
        input.click()
        Thread.sleep(100)
        inputCheckedTestApp.checkKVar.value shouldBe false

        input.getAttribute("checked") shouldBe null
        inputCheckedTestApp.checkKVar.value = true
        Thread.sleep(100)
        input.getAttribute("checked") shouldBe "true"
    }



}

fun main() {
    InputCheckedTestApp()
}

class InputCheckedTestApp {

    internal lateinit var checkKVar : KVar<Boolean>

    val server: Kweb = Kweb(port= 7667) {
        doc.body {
            val i = input(type = InputType.checkbox)
            checkKVar = i.checked()
        }
    }

}

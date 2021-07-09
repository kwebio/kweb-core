import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.SeleniumExtension
import io.kotlintest.shouldBe
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
import org.openqa.selenium.support.ui.Select

@ExtendWith(SeleniumExtension::class)
class SelectValueTest(@Arguments("--headless") private var driver: WebDriver) {

    init {
		//ThreadGuard.protect ensures that the webdriver can only be called by the thread that created it
		//This should make this test thread safe.
        driver = ThreadGuard.protect(driver)
    }

    companion object {
        private lateinit var selectValueTestApp: SelectValueTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            SelectValueTest.selectValueTestApp = SelectValueTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            SelectValueTest.selectValueTestApp.server.close()
        }
    }

    @Test
    fun mainTest() {
        driver.get("http://localhost:7668/")
        val select = Select(driver.findElement<WebElement>(By.tagName("select")))
        selectValueTestApp.selectValue.value shouldBe ""
        Thread.sleep(100)
        select.selectByValue("cat")
        Thread.sleep(100)
        selectValueTestApp.selectValue.value shouldBe "cat"
        select.selectByValue("dog")
        Thread.sleep(100)
        selectValueTestApp.selectValue.value shouldBe "dog"
    }



}

class SelectValueTestApp {

    internal lateinit var selectValue : KVar<String>

    val server: Kweb = Kweb(port= 7668) {
        doc.body {
            val select = select(name = "pets") {
                option().setAttribute("value", "dog").text("Dog")
                option().setAttribute("value", "cat").text("Cat")
            }
            selectValue = select.value
            selectValue.addListener { old, new ->
                println("$old -> $new")
            }
        }
    }

}

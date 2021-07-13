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
class HistoryTest(@Arguments("--headless") private var driver: WebDriver) {

    init {
		//ThreadGuard.protect ensures that the webdriver can only be called by the thread that created it
		//This should make this test thread safe.
        driver = ThreadGuard.protect(driver)
    }

    companion object {
        private lateinit var historyTestApp: HistoryTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            HistoryTest.historyTestApp = HistoryTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            HistoryTest.historyTestApp.server.close()
        }
    }

    @Test
    fun testBackButton() {
        driver.get("http://localhost:7665/")
        val aElement = driver.findElement<WebElement>(By.tagName("a"))
        historyTestApp.url.value shouldBe "/"
        aElement.click()
        Thread.sleep(100)
        historyTestApp.url.value shouldBe "/one"
    }



}

fun main() {
    HistoryTestApp()
}

class HistoryTestApp {

    internal lateinit var url : KVar<String>

    val server: Kweb = Kweb(port= 7665) {
        this@HistoryTestApp.url = this.url
        doc.body {
            route {
                path("/") {
                    a().text("one").on.click {
                        url.value = "/one"
                    }
                }
                path("/one") {
                    a().text("none").on.click {
                        url.value = "/"
                    }
                }
            }

        }
    }

}

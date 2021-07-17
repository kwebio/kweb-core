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

@ExtendWith(SeleniumExtension::class)
class HrefTest(@Arguments("--headless") private var driver: WebDriver) {

    init {
        //ThreadGuard.protect ensures that the webdriver can only be called by the thread that created it
        //This should make this test thread safe.
        driver = ThreadGuard.protect(driver)
    }

    companion object {
        private lateinit var hrefTestApp: HrefTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            HrefTest.hrefTestApp = HrefTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            HrefTest.hrefTestApp.server.close()
        }
    }

    @Test
    fun testClick() {
        driver.get("http://localhost:7665/")
        val aElement = driver.findElement<WebElement>(By.tagName("a"))
        hrefTestApp.clicked.value shouldBe false
        aElement.click()
        Thread.sleep(100)
        hrefTestApp.clicked.value shouldBe true
    }


}

fun main() {
    HrefTestApp()
}

class HrefTestApp {

    internal val clicked: KVar<Boolean> = KVar(false)

    val server: Kweb = Kweb(port = 7665) {
        doc.body {
            route {
                path("/") {
                    a().let { a ->
                        a.href = "/two"
                        a.text("one")
                    }
                }
                path("/two") {

                }
            }
        }

    }
}

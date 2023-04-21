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
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ThreadGuard

@ExtendWith(SeleniumJupiter::class)
class HrefTest {

    companion object {
        private lateinit var hrefTestApp: HrefTestApp

        init {
            System.setProperty("webdriver.http.factory", "jdk-http-client")
        }

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            hrefTestApp = HrefTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            hrefTestApp.server.close()
        }

        @Options
        var chromeOptions = ChromeOptions().apply {
            addArguments("--headless=new")
        }

    }

    @Test
    fun testClick(driver : ChromeDriver) {
        driver.get("http://localhost:7665/")
        val aElement = driver.findElement(By.tagName("a"))
        hrefTestApp.appUrl.value shouldBe "/"
        hrefTestApp.renderCount.value shouldBe 1
        aElement.click()
        Awaitility.await().untilAsserted { hrefTestApp.appUrl.value shouldBe "/two" }
        // Page shouldn't have been re-rendered for a relative link
        Awaitility.await().untilAsserted { hrefTestApp.renderCount.value shouldBe 1 }
    }


}

fun main() {
    HrefTestApp()
}

class HrefTestApp {

    lateinit var appUrl: KVar<String>

    val renderCount = KVar(0)

    val server: Kweb = Kweb(port = 7665) {
        appUrl = this.url
        doc.body {
            renderCount.value++
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

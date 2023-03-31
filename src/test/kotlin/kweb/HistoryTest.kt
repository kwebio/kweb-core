package kweb

import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.DriverCapabilities
import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.github.bonigarcia.wdm.WebDriverManager
import io.kotest.matchers.shouldBe
import kweb.*
import kweb.state.KVar
import kweb.state.render
import org.awaitility.Awaitility.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.Capabilities
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ThreadGuard


@ExtendWith(SeleniumJupiter::class)
class HistoryTest {

    companion object {
        init {
            System.setProperty("webdriver.http.factory", "jdk-http-client")
        }

        @Options
        var chromeOptions = ChromeOptions().apply {
            addArguments("--headless=new")
        }

        private lateinit var historyTestApp: HistoryTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            historyTestApp = HistoryTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            historyTestApp.server.close()
        }
    }

    @Test
    fun testBackButton(@Arguments("--headless") driver : WebDriver) {
        WebDriverManager.chromedriver().setup()

        historyTestApp.reloadCount.value shouldBe 0
        driver.get("http://localhost:7665/0")

        historyTestApp.url.value shouldBe "/0"
        driver.findElement(By.tagName("a")).click()

        await().untilAsserted { historyTestApp.url.value shouldBe "/1" }

        await().pollInSameThread().untilAsserted {
            driver.findElement(By.tagName("a")).click()
        }

        await().untilAsserted { historyTestApp.url.value shouldBe "/2" }
        await().untilAsserted { historyTestApp.reloadCount.value shouldBe 1 }

        driver.navigate().back()

        await().untilAsserted { historyTestApp.url.value shouldBe "/1" }
        await().untilAsserted { historyTestApp.reloadCount.value shouldBe 1 }

        driver.navigate().forward()

        await().untilAsserted { historyTestApp.url.value shouldBe "/2" }
        await().untilAsserted { historyTestApp.reloadCount.value shouldBe 1 }
    }
}

fun main() {
    HistoryTestApp()
}

class HistoryTestApp {

    internal lateinit var url: KVar<String>

    val reloadCount = KVar(0)

    val server: Kweb = Kweb(port = 7665) {
        reloadCount.value++
        this@HistoryTestApp.url = this.url
        doc.body {
            route {
                path("/{num}") { p ->
                    render(p["num"]!!.toInt()) { num ->
                        a { aElement ->
                            with(aElement) {
                                href = "/${num + 1}"
                                text("Next ($num)")
                            }
                        }
                    }
                }
            }
        }
    }
}

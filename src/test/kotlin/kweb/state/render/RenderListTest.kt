package kweb.state.render

import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumExtension
import io.kotlintest.matchers.types.shouldNotBeNull
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
class RenderListTest {
    companion object {
        private lateinit var renderListTestApp: RenderListTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            renderListTestApp = RenderListTestApp()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            renderListTestApp.server.close()
        }

        //selenium-jupiter will automatically fall back if the first browser it tries doesn't work
        //https://bonigarcia.github.io/selenium-jupiter/#generic-driver
        @Options
        var chromeOptions = ChromeOptions().apply {
            //setHeadless(true)
        }

        @Options
        var firefoxOptions = FirefoxOptions().apply {
            //setHeadless(true)
        }
    }

    @Test
    fun initialRender(driver : WebDriver) {
        driver.get("http://localhost:7659/")
        Thread.sleep(300000)
    }
}

class RenderListTestApp {
    val numEl = KVar(5)
    val server: Kweb = Kweb(port = 7659) {
        doc.body.new {
            render(numEl) {
                ol {
                    for (i in 0..numEl.value) {
                        h1().text("$i")
                    }
                }
            }
            val button = button().text("Hello")
            button.on.click {
                changeNum(numEl.value + 1)
            }
        }
    }

    fun changeNum(newNum: Int) {
        numEl.value = newNum
    }

}
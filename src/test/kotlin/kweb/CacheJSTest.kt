package kweb

import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.SeleniumExtension
import io.kotlintest.shouldBe
import kweb.state.KVar
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@ExtendWith(SeleniumExtension::class)
class CacheJSTest(private var driver: WebDriver) {
//class CacheJSTest(@Arguments("--headless") private var driver: WebDriver) {

    companion object {
        private lateinit var cacheJSApp: CacheJSApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            CacheJSTest.cacheJSApp = CacheJSApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            CacheJSTest.cacheJSApp.server.close()
        }
    }

    //This test just makes sure our cache is populated with a couple sample functions.
    @Test
    fun checkCacheSize() {
        driver.get("http://localhost:7659/")
        val browser = cacheJSApp.returnBrowser()
        browser.executeFromCache("""alert({} + "!!!!!");""","Derek")
        browser.executeFromCache("return {} * {}", 4, 4)
        browser.cachedFunctions.size.shouldBe(2)
    }

    //This test checks to see if our server side cache has the user supplied js string.
    @Test
    fun checkCacheTest() {
        driver.get("http://localhost:7659/")
        val browser = cacheJSApp.returnBrowser()
        browser.executeFromCache("""return {} * {}""", 4, 4)
        val keys = browser.cachedFunctions.keys()
        keys.nextElement().shouldBe("return {} * {}")
    }
}

class CacheJSApp {
    private lateinit var button: ButtonElement

    val server: Kweb = Kweb(port= 7659) {
        doc.body.new {
            button = button()
            button.text("Alert")
            button.setAttributeRaw("autofocus", true)
            button.on.click {
            }
        }
    }

    fun returnBrowser() : WebBrowser{
        return button.browser
    }
}
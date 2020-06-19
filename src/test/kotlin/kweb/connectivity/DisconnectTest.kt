package kweb.connectivity

import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumExtension
import kweb.Kweb
import kweb.button
import kweb.new
import kweb.state.KVar
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.Command
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory


/**
 * Test Kweb's behavior when the client has a bad connection
 * I haven't been able to find instructions for simulating network conditions for any driver except chrome. However,
 * using devtools to disable the network connection has no effect on websockets, see this chromium bug:
 * https://bugs.chromium.org/p/chromium/issues/detail?id=423246
 * So we can't currently build these tests :(
 */
@ExtendWith(SeleniumExtension::class)
class DisconnectTest {
    companion object {
        private lateinit var server: ClickerServer

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            server = ClickerServer()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            server.close()
        }

        @Options
        var chromeOptions = ChromeOptions().apply {
            //setHeadless(true)
        }

        @Options
        var firefoxOptions = FirefoxOptions().apply {
            setHeadless(true)
        }
    }

    @Test
    fun pageWorks(driver:WebDriver){
        val site = ClickerSite(driver)
        site.allElementsExist().shouldBeTrue()
        site.getCurrentClicks().shouldBeEqualTo(0)
        site.clickButton()
        site.getCurrentClicks().shouldBeEqualTo(1)
    }

    @Test
    fun noResponseWhileDisconnected(driver:ChromeDriver){
        val site = ClickerSite(driver)
        site.allElementsExist().shouldBeTrue()

        driver.setNetworkConnected(false)

        site.getCurrentClicks().shouldBeEqualTo(0)
        site.clickButton()
        //will always fail, because websockets respond even when network is disabled through devtools
        site.getCurrentClicks().shouldBeEqualTo(0)
    }
}

class ClickerServer {
    private val counter = KVar(0)
    private val server : Kweb = Kweb(port = 7659, debug = true, buildPage = {
        doc.body.new {
            button().text(counter.map{"$it"}).apply {
                on.click {
                    counter.value++
                }
            }
        }
    })

    fun close(){
        server.close()
    }
}

class ClickerSite(driver: WebDriver){
    @FindBy(tagName = "button")
    val button: WebElement? = null

    fun allElementsExist(): Boolean {
        return button?.isDisplayed ?: false
    }

    fun clickButton() {
        button!!.click()
    }

    fun getCurrentClicks(): Int {
        return button!!.text.toInt()
    }

    init {
        driver.get("http://localhost:7659")
        PageFactory.initElements(driver, this)
    }
}

fun ChromeDriver.setNetworkConnected(connected:Boolean){
    val settings = mapOf("offline" to !connected,
                        "latency" to 5,
                        "download_throughput" to 5000,
                        "upload_throughput" to 5000)
    val command = Command(this.sessionId, "setNetworkConditions", mapOf("network_conditions" to settings))
    this.commandExecutor.execute(command)
}
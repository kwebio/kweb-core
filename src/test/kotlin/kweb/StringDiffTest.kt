package kweb

import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.kotest.matchers.shouldBe
import kweb.*
import kweb.state.KVar
import org.awaitility.Awaitility
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ThreadGuard

@ExtendWith(SeleniumJupiter::class)
class StringDiffTest(@Arguments("--headless") unprotectedDriver: ChromeDriver) {

    companion object {
        init {
            System.setProperty("ChromeDriver.http.factory", "jdk-http-client")
        }

        @Options
        var chromeOptions = ChromeOptions().apply {
            addArguments("--headless=new")
        }
    }

    //ThreadGuard.protect ensures that the ChromeDriver can only be called by the thread that created it
    //This should make this test thread safe.
    //val driver: ChromeDriver = ThreadGuard.protect(unprotectedDriver)

    private lateinit var stringDiffTestApp: StringDiffTestApp

    @BeforeEach
    fun setupServer() {
        stringDiffTestApp = StringDiffTestApp()
    }

    @AfterEach
    fun tearDownServer() {
        stringDiffTestApp.server.close()
    }

    @Test
    fun appendTextToBeginning(driver : ChromeDriver) {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement(By.tagName("input"))
        inputField.sendKeys("${Keys.HOME}Super ")
        await().pollInSameThread().untilAsserted {
            inputField.getAttribute("value") shouldBe "Super Lazy Brown Fox"
        }
    }

    @Test
    fun appendTextToMiddle(driver : ChromeDriver) {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement(By.tagName("input"))
        inputField.sendKeys("${Keys.LEFT}${Keys.LEFT}1234")
        await().pollInSameThread().untilAsserted {
            inputField.getAttribute("value") shouldBe "Lazy Brown F1234ox"
        }
    }

    @Test
    fun appendTextToEnd(driver : ChromeDriver) {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement(By.tagName("input"))
        inputField.sendKeys(" Jumped")
        await().pollInSameThread().untilAsserted {
            inputField.getAttribute("value") shouldBe "Lazy Brown Fox Jumped"
        }
    }

    @Test
    fun removeTextFromBeginning(driver : ChromeDriver) {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement(By.tagName("input"))
        inputField.sendKeys("${Keys.HOME}${Keys.DELETE}${Keys.DELETE}${Keys.DELETE}${Keys.DELETE}${Keys.DELETE}")
        await().pollInSameThread().untilAsserted {
            inputField.getAttribute("value") shouldBe "Brown Fox"
        }
    }

    @Test
    fun removeTextFromMiddle(driver : ChromeDriver) {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement(By.tagName("input"))
        inputField.sendKeys("${Keys.LEFT}${Keys.BACK_SPACE}${Keys.BACK_SPACE}")
        await().pollInSameThread().untilAsserted {
            inputField.getAttribute("value") shouldBe "Lazy Brown x"
        }
    }

    @Test
    fun removeTextFromEnd(driver : ChromeDriver) {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement(By.tagName("input"))
        inputField.sendKeys(Keys.END)
        for (i in 0 until 4) {
            inputField.sendKeys(Keys.BACK_SPACE)
        }
        inputField.sendKeys(Keys.ENTER)
        await().pollInSameThread().untilAsserted {
            inputField.getAttribute("value") shouldBe "Lazy Brown"
        }
    }

    @Test
    fun modifyTextOnServerAndVerifyChangeInBrowser(driver : ChromeDriver) {
        driver.get("http://localhost:7660/")
        val inputField = driver.findElement(By.tagName("input"))
        val currentValue = stringDiffTestApp.inputString.value
        stringDiffTestApp.inputString.value = "Super Fox"
        await().pollInSameThread().untilAsserted {
            inputField.getAttribute("value") shouldBe "Super Fox"
        }
        stringDiffTestApp.inputString.value = currentValue
    }
}

class StringDiffTestApp {
    var inputString = KVar("Initial")

    val server: Kweb = Kweb(port = 7660) {
        doc.body {
            val input = input(initialValue = "Lazy Brown Fox")
            inputString = input.value
        }
    }
}



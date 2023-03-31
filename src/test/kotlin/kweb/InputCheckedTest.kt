package kweb

import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.kotest.matchers.shouldBe
import kweb.*
import kweb.state.KVar
import org.awaitility.Awaitility
import org.awaitility.Awaitility.await
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
class InputCheckedTest(@Arguments("--headless") unprotectedDriver: ChromeDriver) {

    companion object {
        private lateinit var inputCheckedTestApp: InputCheckedTestApp

        init {
            System.setProperty("ChromeDriver.http.factory", "jdk-http-client")
        }

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            inputCheckedTestApp = InputCheckedTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            inputCheckedTestApp.server.close()
        }

        @Options
        var chromeOptions = ChromeOptions().apply {
            addArguments("--headless=new")
        }

    }

    @Test
    fun checkBeforeAndAfterClick(driver : ChromeDriver) {
        driver.get("http://localhost:7660/")
        val input = driver.findElement(By.tagName("input"))
        inputCheckedTestApp.checkKVar.value shouldBe false
        input.click()
        await().untilAsserted { inputCheckedTestApp.checkKVar.value shouldBe true }
        input.click()
        await().untilAsserted { inputCheckedTestApp.checkKVar.value shouldBe false }

        await().untilAsserted { input.getAttribute("checked") shouldBe null }
        inputCheckedTestApp.checkKVar.value = true
        await().untilAsserted { input.getAttribute("checked") shouldBe "true" }
    }
}

fun main() {
    InputCheckedTestApp()
}

class InputCheckedTestApp {

    internal lateinit var checkKVar: KVar<Boolean>

    val server: Kweb = Kweb(port = 7660) {
        doc.body {
            val i = input(type = InputType.checkbox)
            checkKVar = i.checked()
        }
    }
}

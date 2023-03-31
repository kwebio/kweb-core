package kweb

import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.kotest.matchers.shouldBe
import kweb.*
import kweb.state.KVar
import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ThreadGuard

@ExtendWith(SeleniumJupiter::class)
class TextAreaInitialValueTest {

    companion object {
        private lateinit var textareaInitialValueTestApp: TextAreaInitialValueTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            textareaInitialValueTestApp = TextAreaInitialValueTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            textareaInitialValueTestApp.server.close()
        }

        init {
            System.setProperty("ChromeDriver.http.factory", "jdk-http-client")
        }

        @Options
        var chromeOptions = ChromeOptions().apply {
            addArguments("--headless=new")
        }
    }

    @Test
    fun mainTest(driver : ChromeDriver) {
        driver.get("http://localhost:7668/")
        Awaitility.await().untilAsserted { textareaInitialValueTestApp.textAreaValue.value shouldBe "cat" }
    }
}

class TextAreaInitialValueTestApp {

    internal lateinit var textAreaValue: KVar<String>

    val server: Kweb = Kweb(port = 7668) {
        doc.body {
            val textArea = textArea(attributes = emptyMap(), initialValue = "cat")
            textAreaValue = textArea.value
            textAreaValue.addListener { old, new ->
                println("$old -> $new")
            }
        }
    }
}

package kweb

import io.github.bonigarcia.seljup.Arguments
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
import org.openqa.selenium.support.ThreadGuard

@ExtendWith(SeleniumJupiter::class)
class TextAreaInitialValueTest(@Arguments("--headless") unprotectedDriver: ChromeDriver) {


    //ThreadGuard.protect ensures that the ChromeDriver can only be called by the thread that created it
    //This should make this test thread safe.
    private val driver = ThreadGuard.protect(unprotectedDriver)

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
    }

    @Test
    fun mainTest() {
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

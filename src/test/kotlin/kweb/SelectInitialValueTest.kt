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
class SelectInitialValueTest(@Arguments("--headless") unprotectedDriver: ChromeDriver) {


    //ThreadGuard.protect ensures that the ChromeDriver can only be called by the thread that created it
    //This should make this test thread safe.
    val driver = ThreadGuard.protect(unprotectedDriver)

    companion object {
        private lateinit var selectInitialValueTestApp: SelectInitialValueTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            selectInitialValueTestApp = SelectInitialValueTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            selectInitialValueTestApp.server.close()
        }
    }

    @Test
    fun mainTest() {
        driver.get("http://localhost:7668/")
        Awaitility.await().untilAsserted { selectInitialValueTestApp.selectValue.value shouldBe "cat" }
    }
}

class SelectInitialValueTestApp {

    internal lateinit var selectValue: KVar<String>

    val server: Kweb = Kweb(port = 7668) {
        doc.body {
            val select = select(name = "pets", initialValue = "cat") {
                option().set("value", "dog").text("Dog")
                option().set("value", "cat").text("Cat")
            }
            selectValue = select.value
            selectValue.addListener { old, new ->
                println("$old -> $new")
            }
        }
    }
}

package kweb

import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.kotest.matchers.shouldBe
import kweb.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ThreadGuard

@ExtendWith(SeleniumJupiter::class)
class ImmediateEventTest(@Arguments("--headless") private var unprotectedDriver: ChromeDriver) {

    //ThreadGuard.protect ensures that the ChromeDriver can only be called by the thread that created it
    //This should make this test thread safe
    val driver : WebDriver = ThreadGuard.protect(unprotectedDriver)


    companion object {
        private lateinit var immediateEventTestApp: ImmediateEventTestApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            immediateEventTestApp = ImmediateEventTestApp()
        }

        @JvmStatic
        @AfterAll
        fun tearDownServer() {
            immediateEventTestApp.server.close()
        }
    }

    @Test
    fun checkBeforeAndAfterClick() {
        driver.get("http://localhost:7660/")
        val label = driver.findElement(By.tagName("h1"))
        label.text shouldBe ("Click Me")
        label.click()
        label.text shouldBe ("Clicked!")
    }
}

fun main() {
    ImmediateEventTestApp()
}

class ImmediateEventTestApp {
    private lateinit var label: H1Element

    val server: Kweb = Kweb(port = 7660) {
        doc.body {
            label = h1()
            label.text("Click Me")
            label.onImmediate.click {
                label.text("Clicked!")
            }
        }
    }
}


import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.SeleniumExtension
import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver

/**
 * Well this is just a simple test
 * to show what kweb done
 */
@ExtendWith(SeleniumExtension::class)
class SimpleTest {
    val kweb = Kweb(port = 12243) {
        doc.body.new {
            h1().text("Lorum Ipsum")
        }
    }

    @Test
    fun testWithChrome(@Arguments("--headless") chromeDriver: ChromeDriver) {
        chromeDriver.get("http://localhost:12243")
        val headerText = chromeDriver.findElement(By.tagName("h1")).text
        headerText.shouldBeEqualTo("Lorum Ipsum")
    }
}
import io.github.bonigarcia.seljup.SeleniumExtension
import io.kweb.demos.todo.TodoApp
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.WebDriverWait

/**
 * Test for the todoApp demo
 */
@ExtendWith(SeleniumExtension::class)
class TodoDemoTest {
    companion object {
        lateinit var todoKweb:TodoApp

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            todoKweb = TodoApp()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            todoKweb.server.close()
        }
    }

    @Test
    fun pageRenders(driver:WebDriver){
        driver.get("http://localhost:7659")
        val site = TodoSite(driver)
        site.allElementsExist().shouldBeTrue()
    }

    @Test
    fun enterNewItem(driver:WebDriver){
        val todoItem = "feel like an ocean, warmed by the sun"
        driver.get("http://localhost:7659")
        val site = TodoSite(driver)
        site.addTodoItem(todoItem)
        val itemElem = WebDriverWait(driver, 5).until {
            driver.findElement<WebElement>(
                    By.xpath("//div[contains(text(),'$todoItem') and @class='content']"))
        }
        itemElem?.isDisplayed?.shouldBeTrue()
    }

    @Test
    fun multipleUsers(driver1:WebDriver, driver2:WebDriver){
        val todoItem = "bring me a great big flood"
        driver1.get("http://localhost:7659")
        val site = TodoSite(driver1)

        //make sure we go to the same list the first driver was redirected to
        driver2.get(driver1.currentUrl)

        //after both pages have loaded, add item via first driver
        site.addTodoItem(todoItem)

        //make sure it appears for second driver
        val itemElem = WebDriverWait(driver2, 5).until {
            driver2.findElement<WebElement>(
                    By.xpath("//div[contains(text(),'$todoItem') and @class='content']"))
        }
        itemElem?.isDisplayed?.shouldBeTrue()
    }
}

class TodoSite(driver: WebDriver){

    @FindBy(className = "message")
    val message: WebElement? = null

    @FindBy(tagName = "h1")
    val header: WebElement? = null

    @FindBy(tagName = "input")
    val input: WebElement? = null

    @FindBy(tagName = "button")
    val addButton: WebElement? = null

    fun allElementsExist() : Boolean {
        return message?.isDisplayed ?: false
                && header?.isDisplayed ?: false
                && input?.isDisplayed ?: false
                && addButton?.isDisplayed ?: false
    }

    fun addTodoItem(item:String){
        println("Adding item $item")
        input?.sendKeys(item)
        addButton?.click()
    }

    init {
        PageFactory.initElements(driver, this)
    }
}
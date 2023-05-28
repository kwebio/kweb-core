package kweb.demos.todo

import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.util.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory

/**
 * Test for the todoApp demo
 */
@ExtendWith(SeleniumJupiter::class)
@TestMethodOrder(OrderAnnotation::class)
class TodoDemoTest {
    companion object {

        init {
            System.setProperty("webdriver.http.factory", "jdk-http-client")
        }

        private lateinit var todoKweb: TodoApp

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

        @Options
        var chromeOptions = ChromeOptions().apply {
            addArguments("--headless=new")
        }
    }

    //TODO: Set the title in todo demo to fix this test
    /*@Test
    fun pageRenders(driver:ChromeDriver){
        val site = TodoSite(driver)
        site.allElementsExist().shouldBeTrue()
        val listId = driver.currentUrl.split('/').reversed()[0]
        //site.title!!.text shouldBe("To Do List #$listId")
    }*/

    @Test
    @Order(1)
    fun enterNewItem(driver: ChromeDriver) {
        val todoItem = "Right eyelids closed, both feet behind"
        val site = TodoSite(driver)
        site.addTodoItem(todoItem)
        await().pollInSameThread().untilAsserted {
            site.driver.findElement(By.xpath("//${"div"}[text()='$todoItem']")).isDisplayed shouldBe true
        }
    }

    @Test
    @Order(2)
    fun multipleUsers(driver1: ChromeDriver, driver2: ChromeDriver) {

        val todoItem = "I aim for tomorrow, work on my mind"
        val site = TodoSite(driver1)

        //make sure we go to the same list the first driver was redirected to
        driver2.get(driver1.currentUrl)
        val site2 = TodoSite(driver2)

        //after both pages have loaded, add item via first driver
        site.addTodoItem(todoItem)

        //make sure it appears for second driver
        await().pollInSameThread().untilAsserted {
            site2.driver.findElement(By.xpath("//${"div"}[text()='$todoItem']")).isDisplayed shouldBe true
        }
    }

    @Test
    @Order(3)
    fun deleteItems(driver: ChromeDriver) {
        val firstItem = "We'll be all right"
        val secondItem = "Stay here some time"
        val thirdItem = "This country dog won't die in the city"

        val site = TodoSite(driver)
        site.addTodoItem(firstItem)
        site.addTodoItem(secondItem)
        site.addTodoItem(thirdItem)
        // This line fails occasionally, but I can't figure out why - ian
        site.deleteItemByText(secondItem)

        await().pollInSameThread().untilAsserted {
            val allItems = site.getAllItems()
            allItems.find { it.text == firstItem } shouldNotBe null
            allItems.find { it.text == secondItem } shouldBe null
            allItems.find { it.text == thirdItem } shouldNotBe null
        }
    }

    @Test
    @Order(4)
    fun navigateToNewSite(driver: ChromeDriver) {
        driver.get("http://localhost:7659")
        val firstSiteUrl = driver.currentUrl
        driver.get("http://localhost:7659")
        driver.currentUrl shouldNotBe firstSiteUrl
    }
}

class TodoSite(val driver: ChromeDriver) {

    @FindBy(tagName = "title")
    val title: WebElement? = null

    @FindBy(className = "message")
    val message: WebElement? = null

    @FindBy(tagName = "h1")
    val header: WebElement? = null

    @FindBy(tagName = "input")
    val input: WebElement? = null

    @FindBy(xpath = "//button[text()='Add']")
    val addButton: WebElement? = null

    fun allElementsExist(): Boolean {
        return message?.isDisplayed ?: false
                && header?.isDisplayed ?: false
                && input?.isDisplayed ?: false
                && addButton?.isDisplayed ?: false
    }

    fun addTodoItem(item: String) {
        input?.sendKeys(item)
        addButton?.click()
    }

    fun getAllItems(): List<WebElement> {
        return driver.findElements(By.xpath("//div[@class='item']"))
    }

    fun deleteItemByText(itemText: String) {
        await().pollInSameThread().untilAsserted {
            val items = driver.findElements(By.xpath("//div[@class='item']"))
            val delButton = items.first { it.text.contains(itemText) }.findElement(By.tagName("button"))
            delButton.click()
        }
    }

    init {
        if (!driver.currentUrl.startsWith("http://localhost:7659/lists")) {
            //if not on correct page, navigate there when page object inits
            driver.get("http://localhost:7659")
        }
        PageFactory.initElements(driver, this)
    }
}

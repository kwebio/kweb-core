package kweb.demos.todo

import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.ui.WebDriverWait

/**
 * Test for the todoApp demo
 */
@ExtendWith(SeleniumJupiter::class)
class TodoDemoTest {
    companion object {
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

        //selenium-jupiter will automatically fall back if the first browser it tries doesn't work
        //https://bonigarcia.github.io/selenium-jupiter/#generic-driver
        @Options
        var chromeOptions = ChromeOptions().apply {
            setHeadless(true)
        }

        @Options
        var firefoxOptions = FirefoxOptions().apply {
            setHeadless(true)
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
    fun enterNewItem(driver: ChromeDriver) {
        val todoItem = "Right eyelids closed, both feet behind"
        val site = TodoSite(driver)
        site.addTodoItem(todoItem)
        site.getItemByText(todoItem).isDisplayed shouldBe true
    }

    @Test
    fun multipleUsers(driver1: ChromeDriver, driver2: ChromeDriver) {
        val todoItem = "I aim for tomorrow, work on my mind"
        val site = TodoSite(driver1)

        //make sure we go to the same list the first driver was redirected to
        driver2.get(driver1.currentUrl)
        val site2 = TodoSite(driver2)

        //after both pages have loaded, add item via first driver
        site.addTodoItem(todoItem)

        //make sure it appears for second driver
        site2.getItemByText(todoItem).isDisplayed shouldBe true
    }

    @Test
    fun deleteItems(driver: ChromeDriver) {
        val firstItem = "We'll be all right"
        val secondItem = "Stay here some time"
        val thirdItem = "This country dog won't die in the city"

        val site = TodoSite(driver)
        site.addTodoItem(firstItem)
        site.addTodoItem(secondItem)
        site.addTodoItem(thirdItem)

        site.deleteItemByText(secondItem)

        val allItems = site.getAllItems()
        allItems.find { it.text == firstItem } shouldNotBe null
        allItems.find { it.text == secondItem } shouldBe null
        allItems.find { it.text == thirdItem } shouldNotBe null
    }

    @Test
    @Disabled
    fun navigateToNewSite(driver: ChromeDriver) {
        driver.get("http://localhost:7659")
        val firstSiteUrl = driver.currentUrl
        driver.get("http://localhost:7659")
        driver.currentUrl shouldBe firstSiteUrl
    }
}

class TodoSite(private val driver: ChromeDriver) {

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

    /**
     * Returns the webelement for a single item in the todolist.
     * Waits 5 seconds for it to appear, then throws ElementNotFoundException.
     * itemText cannot contain single quotes (') because those are used in the xpath to delimit the search string
     */
    fun getItemByText(itemText: String): WebElement {
        return WebDriverWait(driver, 5).until {
            driver.findElement(By.xpath("//div[contains(descendant::text(),'$itemText') and @class='item']"))
        }
    }

    fun deleteItemByText(itemText: String) {
        val item = getItemByText(itemText)
        val delButton = item.findElement<WebElement>(By.tagName("button"))
        delButton.click()
    }

    init {
        if (!driver.currentUrl.startsWith("http://localhost:7659/lists")) {
            //if not on correct page, navigate there when page object inits
            driver.get("http://localhost:7659")
        }
        PageFactory.initElements(driver, this)
    }
}

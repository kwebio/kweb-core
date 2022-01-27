package kweb.state.render

import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.Options
import io.github.bonigarcia.seljup.SeleniumExtension
import io.kotlintest.shouldBe
import kweb.*
import kweb.demos.todo.TodoApp
import kweb.state.KVal
import kweb.state.KVar
import kweb.state.ObservableList
import kweb.state.renderEachWIP
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions

@ExtendWith(SeleniumExtension::class)
class RenderEachTest(@Arguments("--headless") private var driver: WebDriver) {

    /*companion object {
        private lateinit var testSite: RenderEachSite

        @JvmStatic
        @BeforeAll
        fun setupServer() {
            testSite = RenderEachSite()
        }

        @JvmStatic
        @AfterAll
        fun teardownServer() {
            testSite.server.close()
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
*/
    @Test
    fun prependItemTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Horse"))

        Kweb(port = 1234, buildPage = {
            doc.body.new {
                renderEachWIP(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Prepend Moose to list")
                    .on.click {
                        animals.add(0, "Moose")
                    }
            }
        })


        Thread.sleep(20000)
        println(animals.getItems())
    }

    @Test
    fun appendItemTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Horse"))

        Kweb(port = 1234, buildPage = {
            doc.body.new {
                renderEachWIP(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Append Moose to list")
                    .on.click {
                        animals.add("Moose")
                    }
            }
        })
        Thread.sleep(20000)
        println(animals.getItems())
    }

    @Test
    fun insertItemTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Horse"))

        Kweb(port = 1234, buildPage = {
            doc.body.new {
                renderEachWIP(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Insert Moose into center")
                    .on.click {
                        animals.add(2, "Moose")
                    }
            }
        })
        Thread.sleep(20000)
        println(animals.getItems())
    }

    @Test
    fun changeItemTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear"))

        Kweb(port = 1234, buildPage = {
            doc.body.new {
                renderEachWIP(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Change Cat to Horse")
                    .on.click {
                        animals.set(1, "Horse")
                    }
            }
        })
        Thread.sleep(20000)
        println(animals.getItems())
    }

    @Test
    fun deleteItemTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear"))

        Kweb(port = 1234, buildPage = {
            doc.body.new {
                renderEachWIP(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Delete Cat")
                    .on.click {
                        animals.removeAt(1)
                        println(animals.getItems())
                    }
            }
        })
        Thread.sleep(20000)
        println(animals.getItems())
    }

    @Test
    fun moveItemTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Moose", "Horse"))

        Kweb(port = 1234, buildPage = {
            doc.body.new {
                renderEachWIP(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Move Horse to center")
                    .on.click {
                        animals.move(4, 2)
                        println(animals.getItems())
                    }
            }
        })
        Thread.sleep(100000)
    }

    @Test
    fun ClearItemsTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Moose", "Horse"))

        val server = Kweb(port = 1234, buildPage = {
            doc.body.new {
                renderEachWIP(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Clear items")
                    .on.click {
                        animals.clear()
                        println("Animals isEmpty: ${animals.isEmpty()}")
                    }
            }
        })
        driver.get("http://localhost:1234")
        val label = driver.findElements<WebElement>(By.tagName("h1"))
        label[0].text shouldBe("Dog")
        server.close()
    }
}

/*
class RenderEachSite(private val driver: WebDriver) {
    val server = Kweb(port = 1234, buildPage = {
        doc.body.new {
            renderEachWIP(animals) { animal ->
                div().new {
                    h1().text(animal)
                }
            }
            button()
                .text("Prepend Moose to list")
                .on.click {
                    animals.add(0, "Moose")
                }
        }
    })
}*/

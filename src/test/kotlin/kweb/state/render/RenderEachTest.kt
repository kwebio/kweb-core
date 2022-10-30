package kweb.state.render

import io.github.bonigarcia.seljup.Arguments
import io.github.bonigarcia.seljup.SeleniumJupiter
import io.kotest.matchers.shouldBe
import kweb.*
import kweb.state.ObservableList
import kweb.state.renderEach
import org.awaitility.Awaitility
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ThreadGuard

@ExtendWith(SeleniumJupiter::class)
class RenderEachTest(@Arguments("--headless") unprotectedDriver: ChromeDriver) {

    //ThreadGuard.protect ensures that the ChromeDriver can only be called by the thread that created it
    //This should make this test thread safe.
    val driver = ThreadGuard.protect(unprotectedDriver)


    /*NOTE: Thread.sleep(50) is used throughout these tests. I had success with values as small as Thread.sleep(1)
    But for consistent success, I left it at 50.
    I believe Thread.sleep(50) is needed to make Selenium wait to read the updated DOM. I do not believe this is a threading problem in Kweb.
    I think it's just an issue with Selenium. This isn't an issue with the server side code being out of sync.
    It's an issue with Selenium, the client, clicking a button, and then giving the server literally 0 time to respond. */

    @Test
    fun prependItemTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Horse"))

        val server = Kweb(port = 1240, buildPage = {
            doc.body {
                renderEach(animals) { animal ->
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

        driver.get("http://localhost:1240")
        val button = driver.findElements(By.tagName("button"))
        button[0].click()
        await().pollInSameThread().untilAsserted {
            val labels = driver.findElements(By.tagName("h1"))
            labels[0].text shouldBe "Moose"
            labels[1].text shouldBe "Dog"
            labels[2].text shouldBe "Cat"
            labels[3].text shouldBe "Bear"
            labels[4].text shouldBe "Horse"
        }
        server.close()
    }

    @Test
    fun appendItemTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Horse"))

        val server = Kweb(port = 1241, buildPage = {
            doc.body {
                renderEach(animals) { animal ->
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

        driver.get("http://localhost:1241")
        val button = driver.findElements(By.tagName("button"))
        button[0].click()
        await().pollInSameThread().untilAsserted {
            val labels = driver.findElements(By.tagName("h1"))
            labels[0].text shouldBe ("Dog")
            labels[1].text shouldBe ("Cat")
            labels[2].text shouldBe ("Bear")
            labels[3].text shouldBe ("Horse")
            labels[4].text shouldBe ("Moose")
        }
        server.close()
    }

    @Test
    fun insertItemTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Horse"))

        val server = Kweb(port = 1242, buildPage = {
            doc.body {
                renderEach(animals) { animal ->
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

        driver.get("http://localhost:1242")
        val button = driver.findElements(By.tagName("button"))
        button[0].click()
        await().pollInSameThread().untilAsserted {
            val labels = driver.findElements(By.tagName("h1"))
            labels[0].text shouldBe "Dog"
            labels[1].text shouldBe "Cat"
            labels[2].text shouldBe "Moose"
            labels[3].text shouldBe "Bear"
            labels[4].text shouldBe "Horse"
        }
        server.close()
    }

    @Test
    fun changeItemTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear"))

        val server = Kweb(port = 1243, buildPage = {
            doc.body {
                renderEach(animals) { animal ->
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

        driver.get("http://localhost:1243")
        driver.findElements(By.tagName("button")).first().click()
        await().pollInSameThread().untilAsserted {
            val labels = driver.findElements(By.tagName("h1"))
            labels[1].text shouldBe "Horse"
        }
        server.close()
    }

    @Test
    fun deleteItemTest() {
        val animals = ObservableList(mutableListOf("Aardvark", "Bear", "Cow", "Dog", "Elephant"))

        val server = Kweb(port = 1244, buildPage = {
            doc.body {
                renderEach(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Delete Bear")//delete element not at an extremity of the list
                    .on.click {
                        animals.removeAt(1)
                    }
                button()
                    .text("Delete Aardvark")//delete first element in the list
                    .on.click {
                        animals.removeAt(0)
                    }
                button()
                    .text("Delete Elephant")//delete last element in list
                    .on.click {
                        animals.removeAt(2)
                    }
            }
        })

        driver.get("http://localhost:1244")
        val button = driver.findElements(By.tagName("button"))
        button[0].click()

        await().pollInSameThread().untilAsserted {
            var labels = driver.findElements(By.tagName("h1"))
            labels[0].text shouldBe "Aardvark"
            labels[1].text shouldBe "Cow"
            labels[2].text shouldBe "Dog"
            labels[3].text shouldBe "Elephant"
        }

        button[1].click()
        await().pollInSameThread().untilAsserted {
            val labels = driver.findElements(By.tagName("h1"))
            labels[0].text shouldBe "Cow"
            labels[1].text shouldBe "Dog"
            labels[2].text shouldBe "Elephant"
        }

        button[2].click()
        await().pollInSameThread().untilAsserted {
            val labels = driver.findElements(By.tagName("h1"))
            labels[0].text shouldBe "Cow"
            labels[1].text shouldBe "Dog"
        }
        server.close()
    }

    @Test
    fun moveItemFromEndToCenterTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Moose", "Horse"))

        val server = Kweb(port = 1245, buildPage = {
            doc.body {
                renderEach(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Move Horse to center")
                    .on.click {
                        animals.move(4, 2)
                    }
            }
        })

        driver.get("http://localhost:1245")
        val button = driver.findElement(By.tagName("button"))
        button.click()
        await().pollInSameThread().untilAsserted {
            val labels = driver.findElements(By.tagName("h1"))
            labels[0].text shouldBe "Dog"
            labels[1].text shouldBe "Cat"
            labels[2].text shouldBe "Horse"
            labels[3].text shouldBe "Bear"
            labels[4].text shouldBe "Moose"
        }
        server.close()
    }

    @Test
    fun moveItemFromStartToEnd() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Moose", "Horse"))

        val server = Kweb(port = 1246, buildPage = {
            doc.body {
                renderEach(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Move Dog to end")
                    .on.click {
                        animals.move(0, 4)
                    }
            }
        })

        driver.get("http://localhost:1246")
        val button = driver.findElement(By.tagName("button"))
        button.click()
       await().pollInSameThread().untilAsserted {
           val labels = driver.findElements(By.tagName("h1"))
           labels[0].text shouldBe "Cat"
           labels[1].text shouldBe "Bear"
           labels[2].text shouldBe "Moose"
           labels[3].text shouldBe "Horse"
           labels[4].text shouldBe "Dog"
       }
        server.close()
    }

    @Test
    fun moveItemFromEndToStart() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Moose", "Horse"))

        val server = Kweb(port = 1247, buildPage = {
            doc.body {
                renderEach(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Move Horse to start")
                    .on.click {
                        animals.move(4, 0)
                    }
            }
        })

        driver.get("http://localhost:1247")
        val button = driver.findElement(By.tagName("button"))
        button.click()
        await().pollInSameThread().untilAsserted {
            val labels = driver.findElements(By.tagName("h1"))
            labels[0].text shouldBe "Horse"
            labels[1].text shouldBe "Dog"
            labels[2].text shouldBe "Cat"
            labels[3].text shouldBe "Bear"
            labels[4].text shouldBe "Moose"
        }
        server.close()
    }

    @Test
    fun ClearItemsTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear", "Moose", "Horse"))

        val server = Kweb(port = 1248, buildPage = {
            doc.body {
                renderEach(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Clear items")
                    .on.click {
                        animals.clear()
                    }
            }
        })

        driver.get("http://localhost:1248")
        val button = driver.findElement(By.tagName("button"))
        button.click()
        await().pollInSameThread().untilAsserted {
            val labels = driver.findElements(By.tagName("h1"))
            labels.size shouldBe 0
        }
        server.close()
    }

    @Test
    fun nestedRenderEachTest() {
        val planets = ObservableList(mutableListOf())
        val jupiter = ObservableList(mutableListOf("Io", "Europa", "Ganymede", "Callisto"))
        val saturn = ObservableList(mutableListOf("Titan", "Rhea", "Enceladus", "Mimas", "Phoebe"))
        val neptune = ObservableList(mutableListOf("Triton", "Nereid", "Neso", "Psamathe", "Galatea", "Proteus"))
        planets.add(jupiter)
        planets.add(saturn)
        planets.add(neptune)

        val server = Kweb(port = 1249, buildPage = {
            doc.body {
                renderEach(planets) { planet ->
                    renderEach(planet as ObservableList<*>) { moon ->
                        h1().text(moon.toString())

                    }
                    button()
                        .text("Move last moon to second from the top")
                        .on.click {
                            planet.move(planet.size - 1, 1)
                        }
                }
                br()
                br()
                button()
                    .text("Reorder planets")
                    .on.click {
                        planets.move(2, 0)
                        planets.move(1, 2)
                    }
            }
        })

        driver.get("http://localhost:1249")
        val buttons = driver.findElements(By.tagName("button"))
        for (button in buttons) {
            button.click()
        }
        Thread.sleep(50)
        val labels = driver.findElements(By.tagName("h1"))
        labels[1].text shouldBe "Proteus"
        labels[7].text shouldBe "Phoebe"
        labels[13].text shouldBe "Europa"
        server.close()
    }
}

package kweb.state.render

import kweb.*
import kweb.state.KVal
import kweb.state.KVar
import kweb.state.ObservableList
import kweb.state.renderEachWIP
import org.junit.jupiter.api.Test

class RenderEachTest {

    /*@Test
    fun initialRenderEachTest() {
        val animals = listOf("Dog", "Cat", "Bear")
        val collection = KVal(animals)

        Kweb(port = 1234, buildPage = {
            doc.body.new {
                renderEachWIP(collection) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
            }
        })
    }*/


    @Test
    fun insertItemTest() {
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear"))

        Kweb(port = 1234, buildPage = {
            doc.body.new {
                renderEachWIP(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Add Moose")
                    .on.click {
                        animals.items += "Moose"
                    }
            }
        })
        Thread.sleep(20000)
        println(animals.items)
    }

    @Test
    fun changeItemTest() {
        val items = listOf("Dog", "Cat", "Bear")
        val animals = ObservableList(mutableListOf("Dog", "Cat", "Bear"))

        Kweb(port = 1234, buildPage = {
            doc.body.new {
                renderEachWIP(animals) { animal ->
                    div().new {
                        h1().text(animal)
                    }
                }
                button()
                    .text("Change Cat to Moose")
                    .on.click {
                        animals.items[1] = "Moose"
                    }
            }
        })
        Thread.sleep(20000)
        println(animals.items)
    }

    @Test
    fun moveItemTest() {
        val items = listOf("Dog", "Cat", "Bear")
    }

    @Test
    fun deleteItemTest() {
        val items = listOf("Dog", "Cat", "Bear")
    }

}
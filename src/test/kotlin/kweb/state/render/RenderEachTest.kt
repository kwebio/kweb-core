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
                        animals.insert(animals.getItems().size, "Moose")
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
                    .text("Change Cat to Moose")
                    .on.click {
                        animals.set(1, "Horse")
                        //animals.change(1, "Moose")
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
                    .text("Move Item")
                    .on.click {
                        animals.move(4, 2)
                        println(animals.getItems())
                    }
            }
        })
        Thread.sleep(100000)
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
                        animals.delete(1)
                        println(animals.getItems())
                    }
            }
        })
        Thread.sleep(20000)
        println(animals.getItems())
    }

}
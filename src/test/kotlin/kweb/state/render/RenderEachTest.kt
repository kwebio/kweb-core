package kweb.state.render

import kweb.*
import kweb.state.KVal
import kweb.state.KVar
import kweb.state.ObservableList
import kweb.state.renderEachWIP
import org.junit.jupiter.api.Test

class RenderEachTest {

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

}
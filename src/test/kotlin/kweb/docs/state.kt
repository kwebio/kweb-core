package kweb.docs

import kweb.*
import kweb.state.*

fun state1() {
    // ANCHOR: createkvar
val counter = KVar(0)
    // ANCHOR_END: createkvar

    // ANCHOR: modifykvar
println("Counter value ${counter.value}")
counter.value = 1
println("Counter value ${counter.value}")
counter.value++
println("Counter value ${counter.value}")
    // ANCHOR_END: modifykvar

    // ANCHOR: mapkvar
val counterDoubled = counter.map { it * 2 }
counter.value = 5
println("counter: ${counter.value}, doubled: ${counterDoubled.value}")
counter.value = 6
println("counter: ${counter.value}, doubled: ${counterDoubled.value}")
    // ANCHOR_END: mapkvar

    // ANCHOR: kvartext
Kweb(port = 2135) {
    doc.body {
        val name = KVar("John")
        li().text(name)
    }
}
    // ANCHOR_END: kvartext


    // ANCHOR: bindinput
Kweb(port = 2395) {
    doc.body {
        p().text("What is your name?")
        val clickMe = input(type = InputType.text)
        val nameKVar = KVar("Peter Pan")
        clickMe.value = nameKVar
        p().text(nameKVar.map { n -> "Hi $n!" })
    }
}
    // ANCHOR_END: bindinput

    // ANCHOR: render1
val list = KVar(listOf("one", "two", "three"))

Kweb(port = 16097) {
    doc.body {
        render(list) { rList ->
            ul {
                for (item in rList) {
                    li().text(item)
                }
            }
        }
    }
}
    // ANCHOR_END: render1

    // ANCHOR: render2
list.value = listOf("four", "five", "six")
    // ANCHOR_END: render2

    fun renderEach1() {
        Kweb(port = 16097) {
            // ANCHOR: rendereach
doc.body {
    val list = ObservableList(listOf("one", "two", "three"))
    ul {
        renderEach(list) { item ->
            li().text(item)
        }
    }
    list.add(1, "one and a half")
    list.removeAt(2)
    list.move(0, 1)
    list[0] = "ONE"
}
            // ANCHOR_END: rendereach
        }
    }

// ANCHOR: dataclass
data class User(val name: String)

val user = KVar(User("Ian"))
val name = user.property(User::name)
name.value = "John"
println(user) // Will print: KVar(User(name = "John"))
// ANCHOR_END: dataclass

fun state2() {
    val counter = KVar(0)
// ANCHOR: reversible1
val counterDoubled = counter.map(object : ReversibleFunction<Int, Int>("doubledCounter") {
    override fun invoke(from: Int) = from * 2
    override fun reverse(original: Int, change: Int) = change / 2
})
counter.value = 5
println("counter: ${counter.value}, doubled: ${counterDoubled.value}")
// output: counter: 5, doubled: 10

counterDoubled.value = 12 // <-- Couldn't do this with a KVal
println("counter: ${counter.value}, doubled: ${counterDoubled.value}")
// output: counter: 6, doubled: 12
// ANCHOR_END: reversible1
    }
}


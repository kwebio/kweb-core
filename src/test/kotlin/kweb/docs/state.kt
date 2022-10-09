package kweb.docs

import kweb.*
import kweb.state.KVar
import kweb.state.ReversibleFunction
import kweb.state.property
import kweb.state.render

fun state1() {
    // ANCHOR: create_kvar
val counter = KVar(0)
    // ANCHOR_END: create_kvar

    // ANCHOR: modify_kvar
println("Counter value ${counter.value}")
counter.value = 1
println("Counter value ${counter.value}")
counter.value++
println("Counter value ${counter.value}")
    // ANCHOR_END: modify_kvar

    // ANCHOR: map_kvar
val counterDoubled = counter.map { it * 2 }
counter.value = 5
println("counter: ${counter.value}, doubled: ${counterDoubled.value}")
counter.value = 6
println("counter: ${counter.value}, doubled: ${counterDoubled.value}")
    // ANCHOR END: map_kvar

    // ANCHOR: kvar_text
Kweb(port = 2135) {
    doc.body {
        val name = KVar("John")
        li().text(name)
    }
}
    // ANCHOR END: kvar_text


    // ANCHOR: bind_input
Kweb(port = 2395) {
    doc.body {
        p().text("What is your name?")
        val clickMe = input(type = InputType.text)
        val nameKVar = KVar("Peter Pan")
        clickMe.value = nameKVar
        p().text(nameKVar.map { n -> "Hi $n!" })
    }
}
    // ANCHOR END: bind_input

    // ANCHOR: render_1
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
    // ANCHOR END: render_1

    // ANCHOR: render_2
list.value = listOf("four", "five", "six")
    // ANCHOR END: render_2

    // ANCHOR: data_class
data class User(val name: String)

val user = KVar(User("Ian"))
val name = user.property(User::name)
name.value = "John"
println(user) // Will print: KVar(User(name = "John"))
    // ANCHOR END: data_class

}

fun state2() {
    val counter = KVar(0)
// ANCHOR: reversible_1
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
// ANCHOR END: reversible_1
    }

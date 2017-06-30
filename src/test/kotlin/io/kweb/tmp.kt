package io.kweb

/**
 * Created by ian on 6/30/17.
 */


data class Foo(val k : Int)

fun render(f : (Foo).() -> Unit) {
    f.invoke(Foo(1241))
}

fun main(args: Array<String>) {
    Foo(1010).apply {
        render {
            println(this)
        }
    }
}
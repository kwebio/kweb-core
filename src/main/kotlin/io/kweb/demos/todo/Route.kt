package io.kweb.demos.todo

object Route {
    sealed class TodoPath {
        class Root : TodoPath()
        data class Lists(val uid : String) : TodoPath()
    }
}
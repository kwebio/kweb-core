package io.kweb.demos.estimate

object State {
    data class User(val id : String, val token : String, val name : String)

    data class List(val id : String, val title : String)

    data class Item(val id : String, val name : String)

    data class Measure(val id : String, val name : String, val listId : String)

    data class Vote(val id : String, val user : String, val item1 : String, val item2 : String, val measure : String, val proportionateValue : Double)
}
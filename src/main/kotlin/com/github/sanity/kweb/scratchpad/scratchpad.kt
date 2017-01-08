package com.github.sanity.kweb.scratchpad

import com.github.sanity.kweb.clientConduits.WebsocketsClientConduit
import com.github.sanity.kweb.dom.Element

fun main(args: Array<String>) {
    WebsocketsClientConduit(8091) { // TODO: This JavaScript should be sent down with the page
        with(doc.body) {
            html {
                h1("Simple KWeb demo")
                with(ul()) {
                    val todoItems = ArrayList<Element>()
                    todoItems += li().text("one")
                    todoItems += li().text("two")
                            .on.click {
                        println("Two was clicked")
                        false // TODO: Remove requirement for this in callbacks, handle removing callback for future somewhere else
                    }
                    todoItems += li().text("three")

                    on.click {
                        println("List was clicked")
                        false
                    }
                }

            }
        }
        false
    }
    Thread.sleep(10000)
}

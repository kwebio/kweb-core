package com.github.sanity.kweb.scratchpad

import com.github.sanity.kweb.clientConduits.WebsocketsClientConduit
import com.github.sanity.kweb.dom.Element
import com.github.sanity.kweb.dom.Element.InputType.text
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    WebsocketsClientConduit(8091) {
        thread {
            val body = doc.body.apply {
                h1("Simple KWeb demo")
                fun newListItem(ul: Element.ULElement, text: String) {
                    val li = ul.li()
                    li.text(text)
                            .on.click { li.delete() }
                }

                val ul = ul().apply {
                    for (text in listOf("one", "two", "three")) {
                        newListItem(this, text)
                    }
                }
                val input = input(type = text, size = 20)
                button().text("Add Item")
                        .on.click {
                    async {
                        newListItem(ul, input.getValue().await())
                        input.setValue("")
                    }
                }
            }
        }
        false
    }
    Thread.sleep(10000)
}


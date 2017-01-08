package com.github.sanity.kweb.scratchpad

import com.github.sanity.kweb.clientConduits.WebsocketsClientConduit
import kotlinx.coroutines.async
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    WebsocketsClientConduit(8091) {
        thread {
            async {
                var clicks = 0
                val heading = doc.body.appendChild("h1")
                heading.setInnerHTML("0 clicks")
                heading.addEventListener("click") {
                    clicks++
                    heading.setInnerHTML("$clicks clicks")
                    false
                }
            }
        }
        false
    }
    Thread.sleep(10000)
}

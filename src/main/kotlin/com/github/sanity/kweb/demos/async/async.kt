package com.github.sanity.kweb.demos.async

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.element.creation.createElement
import com.github.sanity.kweb.dom.element.modification.setAttribute
import com.github.sanity.kweb.dom.element.modification.setText
import com.github.sanity.kweb.dom.element.read.read
import kotlinx.coroutines.async
import kotlinx.coroutines.await

/**
 * Created by ian on 1/11/17.
 */
fun main(args: Array<String>) {
    println("Visit http://127.0.0.1:8091/ in your browser...")
    KWeb(8091) {
        doc.body.apply {
            val p1 = createElement("p").setAttribute("id", 1).setText("one")
            val p2 = createElement("p").setAttribute("id", 2).setText("two")
            val p3 = createElement("p").setAttribute("id", 3).setText("three")
            async {
                println("Synchronous retrieve")
                val syncStartTime = System.nanoTime()
                println("${p1.read.innerHtml.await()} ${p2.read.innerHtml.await()} ${p3.read.innerHtml.await()}")
                val syncTime = System.nanoTime() - syncStartTime
                println("Time elapsed: ${syncTime}ns")
                println("Asynchronous retrieve")
                val asyncStartTime = System.nanoTime()
                val p1Future = p1.read.innerHtml
                val p2Future = p2.read.innerHtml
                val p3Future = p3.read.innerHtml
                println("${p1Future.await()} ${p2Future.await()} ${p3Future.await()}")
                val asyncTime = System.nanoTime() - asyncStartTime
                println("Time elapsed: ${asyncTime}ns")
                println("${(syncTime.toDouble() / asyncTime)} times faster")
                System.exit(0)
            }
        }
    }
}
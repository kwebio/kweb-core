package com.github.sanity.kweb

import java.util.concurrent.CompletableFuture

class CoreReceiver(private val clientId: String, private val cc: ClientConduit, val response: String? = null) {
    fun execute(js: String) {
        cc.execute(clientId, js)
    }

    fun executeWithCallback(js: String, callbackId: Int, callback: (String) -> Unit) {
        cc.executeWithCallback(clientId, js, callbackId, callback)
    }

    fun evaluate(js: String): CompletableFuture<String> {
        val cf = CompletableFuture<String>()
        evaluateWithCallback(js) {
            cf.complete(response)
            false
        }
        return cf
    }


    fun evaluateWithCallback(js: String, rh: CoreReceiver.() -> Boolean) {
        cc.evaluate(clientId, js, { rh.invoke(CoreReceiver(clientId, cc, it)) })
    }

    val doc = Document(this)
}
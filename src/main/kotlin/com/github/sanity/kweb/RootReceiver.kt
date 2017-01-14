package com.github.sanity.kweb

import com.github.sanity.kweb.dom.Document
import com.github.sanity.kweb.plugins.KWebPlugin
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

class RootReceiver(private val clientId: String, internal val cc: ClientConduit, val response: String? = null) {
    private val plugins: Set<KClass<KWebPlugin>> by lazy {
        cc.plugins.map { it::class }.toSet()
    }

    internal fun require(vararg requiredPlugins: KClass<out KWebPlugin>) {
        val missing = java.util.HashSet<String>()
        for (requiredPlugin in requiredPlugins) {
            if (!plugins.contains(requiredPlugin)) missing.add(requiredPlugin.simpleName ?: requiredPlugin.jvmName)
        }
        if (missing.isNotEmpty()) {
            throw RuntimeException("Plugin(s) ${missing.joinToString(separator = ", ")} required but not passed to KWeb constructor")
        }
    }

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


    fun evaluateWithCallback(js: String, rh: RootReceiver.() -> Boolean) {
        cc.evaluate(clientId, js, { rh.invoke(RootReceiver(clientId, cc, it)) })
    }

    val doc = Document(this)
}
package io.kweb

import io.kweb.Server2ClientMessage.Instruction
import io.kweb.dom.Document
import io.kweb.plugins.KWebPlugin
import io.kweb.state.*
import io.mola.galimatias.URL
import mu.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * A conduit for communicating with a remote web browser, can be used to execute JavaScript and evaluate JavaScript
 * expressions and retrieve the result.
 */

val logger = KotlinLogging.logger {}

class WebBrowser(private val sessionId: String, val httpRequestInfo: HttpRequestInfo, internal val kweb: Kweb) {

    val idCounter = AtomicInteger(0)

    fun generateId() : String = idCounter.getAndIncrement().toString(36)

    private val plugins: Map<KClass<out KWebPlugin>, KWebPlugin> by lazy {
        kweb.appliedPlugins.map { it::class to it }.toMap()
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <P : KWebPlugin> plugin(plugin: KClass<out P>): P {
        return (plugins[plugin] ?: throw RuntimeException("Plugin $plugin is missing")) as P
    }

    internal fun require(vararg requiredPlugins: KClass<out KWebPlugin>) {
        val missing = java.util.HashSet<String>()
        for (requiredPlugin in requiredPlugins) {
            if (!plugins.contains(requiredPlugin)) missing.add(requiredPlugin.simpleName ?: requiredPlugin.jvmName)
        }
        if (missing.isNotEmpty()) {
            throw RuntimeException("Plugin(s) ${missing.joinToString(separator = ", ")} required but not passed to Kweb constructor")
        }
    }

    fun execute(js: String) {
        kweb.execute(sessionId, js)
    }

    fun executeWithCallback(js: String, callbackId: Int, callback: (String) -> Unit) {
        kweb.executeWithCallback(sessionId, js, callbackId, callback)
    }

    fun removeCallback(callbackId: Int) {
        kweb.removeCallback(sessionId, callbackId)
    }

    fun evaluate(js: String): CompletableFuture<String> {
        val cf = CompletableFuture<String>()
        evaluateWithCallback(js) { response ->
            cf.complete(response)
            false
        }
        return cf
    }

    fun evaluateWithCallback(js: String, rh: (String) -> Boolean) {
        kweb.evaluate(sessionId, js) { rh.invoke(it) }
    }

    fun send(instruction: Instruction) = send(listOf(instruction))

    fun send(instructions: List<Instruction>) {
        kweb.send(sessionId, instructions)
    }

    val doc = Document(this)

    // Note: It's important that we only have one KVar for the URL for this WebBrowser to ensure that changes
    //       propagate everywhere they should.  That's why it's lazy.
    val url: KVar<String>
            by lazy {
                val url = KVar(httpRequestInfo.requestedUrl)

                url.addListener { old, new ->
                    pushState(new)
                }

                url
            }

    fun pushState(path: String) {
        val url = URL.parse(path)
        execute("""
        history.pushState({}, "", location.origin+"${url.path()}");
        """.trimIndent())
    }

    fun <T : Any> url(mapper: (String) -> T) = url.map(mapper)

    fun <T : Any> url(func: ReversableFunction<String, T>) = url.map(func)
}

private fun intToByteArray(value: Int): ByteArray {
    return byteArrayOf(value.ushr(24).toByte(), value.ushr(16).toByte(), value.ushr(8).toByte(), value.toByte())
}
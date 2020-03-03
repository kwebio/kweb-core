package io.kweb

import io.kweb.client.HttpRequestInfo
import io.kweb.client.Server2ClientMessage.Instruction
import io.kweb.dom.Document
import io.kweb.plugins.KwebPlugin
import io.kweb.state.*
import io.mola.galimatias.URL
import mu.KotlinLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.*
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * A conduit for communicating with a remote web browser, can be used to execute JavaScript and evaluate JavaScript
 * expressions and retrieveJs the result.
 */

val logger = KotlinLogging.logger {}

class WebBrowser(private val sessionId: String, val httpRequestInfo: HttpRequestInfo, internal val kweb: Kweb) {

    val idCounter = AtomicInteger(0)

    /**
     * During page render, the initial HTML document will be available for modifiation as a
     * [JSoup Document](https://jsoup.org/) in this [AtomicReference].
     *
     * Callers to [execute] may check for this being non-null, and if so edit the document
     * *instead* of some or all of the JavaScript they must call.
     *
     * The purpose of this is to implement Server-Side Rendering.
     */
    val htmlDocument = AtomicReference<org.jsoup.nodes.Document?>(null)

    fun generateId() : String = idCounter.getAndIncrement().toString(36)

    private val plugins: Map<KClass<out KwebPlugin>, KwebPlugin> by lazy {
        kweb.appliedPlugins.map { it::class to it }.toMap()
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <P : KwebPlugin> plugin(plugin: KClass<out P>): P {
        return (plugins[plugin] ?: error("Plugin $plugin is missing")) as P
    }

    internal fun require(vararg requiredPlugins: KClass<out KwebPlugin>) {
        val missing = java.util.HashSet<String>()
        for (requiredPlugin in requiredPlugins) {
            if (!plugins.contains(requiredPlugin)) missing.add(requiredPlugin.simpleName ?: requiredPlugin.jvmName)
        }
        if (missing.isNotEmpty()) {
            error("Plugin(s) ${missing.joinToString(separator = ", ")} required but not passed to Kweb constructor")
        }
    }

    fun execute(js: String) {
        kweb.execute(sessionId, js)
    }

    fun executeWithCallback(js: String, callbackId: Int, callback: (Any) -> Unit) {
        kweb.executeWithCallback(sessionId, js, callbackId, callback)
    }

    fun removeCallback(callbackId: Int) {
        kweb.removeCallback(sessionId, callbackId)
    }

    fun evaluate(js: String): CompletableFuture<Any> {
        val cf = CompletableFuture<Any>()
        evaluateWithCallback(js) { response ->
            cf.complete(response)
            false
        }
        return cf
    }

    fun evaluateWithCallback(js: String, rh: (Any) -> Boolean) {
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

                url.addListener { _, newState ->
                    pushState(newState)
                }

                url
            }

    private fun pushState(path: String) {
        val url = URL.parse(path).path()
        execute("""
        history.pushState({}, "", "$url");
        """.trimIndent())
    }

    fun <T : Any> url(mapper: (String) -> T) = url.map(mapper)

    fun <T : Any> url(func: ReversableFunction<String, T>) = url.map(func)
}

private fun intToByteArray(value: Int): ByteArray {
    return byteArrayOf(value.ushr(24).toByte(), value.ushr(16).toByte(), value.ushr(8).toByte(), value.toByte())
}

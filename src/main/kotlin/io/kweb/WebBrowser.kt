package io.kweb

import io.kweb.dom.Document
import io.kweb.plugins.KWebPlugin
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import mu.KLogging
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * A conduit for communicating with a remote web browser, can be used to execute JavaScript and evaluate JavaScript
 * expressions and retrieve the result.
 */
class WebBrowser(private val clientId: String, val httpRequestInfo: HttpRequestInfo, internal val cc: Kweb, val response: String? = null) {
    companion object: KLogging()

    private val plugins: Map<KClass<out KWebPlugin>, KWebPlugin> by lazy {
        cc.appliedPlugins.map { it::class to it }.toMap()
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <P : KWebPlugin> plugin(plugin : KClass<out P>) : P {
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


    fun evaluateWithCallback(js: String, rh: WebBrowser.() -> Boolean) {
        cc.evaluate(clientId, js, { rh.invoke(WebBrowser(clientId, httpRequestInfo, cc, it)) })
    }

    val doc = Document(this)

    fun async(doWork: suspend () -> Unit) {
        launch(CommonPool) {
            try {
                doWork()
            } catch (t: Throwable) {
                val sb = StringBuilder()
                sb.appendln("Exception thrown in Kweb async {} block:\n$t")
                t.stackTrace.pruneAndDumpStackTo(sb)
                logger.error(sb.toString())
            }
        }
    }
}

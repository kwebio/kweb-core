package kweb

import io.mola.galimatias.URL
import kweb.client.HttpRequestInfo
import kweb.html.Document
import kweb.html.HtmlDocumentSupplier
import kweb.plugins.KwebPlugin
import kweb.state.KVar
import kweb.state.ReversibleFunction
import kweb.util.pathQueryFragment
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * A conduit for communicating with a remote web browser, can be used to execute JavaScript and evaluate JavaScript
 * expressions and retrieveJs the result.
 */

private val logger = KotlinLogging.logger {}

class WebBrowser(private val sessionId: String, val httpRequestInfo: HttpRequestInfo, internal val kweb: Kweb) {

    private val idCounter = AtomicInteger(0)

    /**
     * During page render, the initial HTML document will be available for modification as a
     * [JSoup Document](https://jsoup.org/) in this [AtomicReference].
     *
     * Callers to [execute] may check for this being non-null, and if so edit the document
     * *instead* of some or all of the JavaScript they must call.
     *
     * The purpose of this is to implement Server-Side Rendering.
     */
    val htmlDocument = AtomicReference<org.jsoup.nodes.Document?>(null)

    fun generateId(): String = idCounter.getAndIncrement().toString(36)

    val cachedFunctions = ConcurrentHashMap<String, Int>()

    private val plugins: Map<KClass<out KwebPlugin>, KwebPlugin> by lazy {
        HtmlDocumentSupplier.appliedPlugins.map { it::class to it }.toMap()
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

    data class JSFunction(val js: String, val params: String)
    //this function substitutes "{}" in user supplied javascript, for randomly generated variable names
    private fun getJsFunction(rawJs: String): JSFunction {
        val rng = Random()
        var js = rawJs
        val params = StringBuilder()
        while (js.contains("{}")) {
            //a few random letters, an underscore, and a randomly generated number should make a variable name that no one would ever come up with
            val jsVarName = "rzd_${rng.nextInt(1000)}"
            js = js.replaceFirst("{}", jsVarName)
            params.append("$jsVarName,")
        }
        if (params.isNotEmpty()) {
            params.deleteCharAt(params.lastIndex)//delete the last trailing comma
        }
        return JSFunction(js, params.toString())
    }

    fun execute(js: String, vararg args: Any?) {
        cachedFunctions[js]?.let {
            kweb.executeFromCache(sessionId, it, listOf(*args))
        } ?: run {
            val rng = Random()
            val cacheId = rng.nextInt()
            val func = getJsFunction(js)
            //we add the user's unmodified js as a key and the cacheId as it's value in the hashmap
            cachedFunctions[js] = cacheId
            //we send the modified js to the client to be cached there.
            //we don't cache the modified js on the server, because then we'd have to modify JS on the server, everytime we want to check the server's cache
            kweb.cacheAndExecute(sessionId, cacheId, func.js, func.params, listOf(*args))
        }
    }

    fun callJs(js: String, vararg args: Any?) {
        cachedFunctions[js]?.let {
            kweb.executeFromCache(sessionId, it, listOf(*args))
            kweb.callJs(sessionId, cacheId = it, args = listOf(*args), javascript = js)
        } ?: run {
            val rng = Random()
            val cacheId = rng.nextInt()
            val func = getJsFunction(js)
            //we add the user's unmodified js as a key and the cacheId as it's value in the hashmap
            cachedFunctions[js] = cacheId
            //we send the modified js to the client to be cached there.
            //we don't cache the modified js on the server, because then we'd have to modify JS on the server, everytime we want to check the server's cache
            kweb.callJs(sessionId, cacheId = cacheId, args = listOf(*args), javascript = func.js, parameters = func.params)
        }
    }

    fun callJsWithCallback(js: String, callbackId: Int, callback: (Any) -> Unit, vararg args: Any?) {
        cachedFunctions[js]?.let {
            kweb.callJsWithCallback(sessionId, cacheId = it, args = listOf(*args), callbackId = callbackId,
            javascript = js, callback = callback, jsCached = true)
        } ?: run {
            val rng = Random()
            val cacheId = rng.nextInt()
            val func = getJsFunction(js)
            //we add the user's unmodified js as a key and the cacheId as it's value in the hashmap
            cachedFunctions[js] = cacheId
            //we send the modified js to the client to be cached there.
            //we don't cache the modified js on the server, because then we'd have to modify JS on the server, everytime we want to check the server's cache
            kweb.callJsWithCallback(sessionId, cacheId = cacheId, javascript = func.js, parameters = func.params, args = listOf(*args),
            jsCached = false, callbackId = callbackId, callback = callback)
        }
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

    fun evaluateWithCallback(js: String, returnHandler: (Any) -> Boolean) {
        kweb.evaluate(sessionId, js) { returnHandler.invoke(it) }
    }

    val doc = Document(this)

    /**
     * The URL of the page, relative to the origin - so for the page `http://foo/bar?baz#1`, the value would be
     * `/bar?baz#1`.
     *
     * When this KVar is modified the browser will automatically update the URL in the browser along with any DOM
     * elements based on this [url] (this will be handled automatically by [kweb.routing.route]).
     */
    val url: KVar<String>
            by lazy {
                val originRelativeURL = URL.parse(httpRequestInfo.requestedUrl).pathQueryFragment
                val url = KVar(originRelativeURL)

                url.addListener { _, newState ->
                    pushState(newState)
                }

                url
            }

    private fun pushState(url: String) {
        if (!url.startsWith('/')) {
            logger.warn("pushState should only be called with origin-relative URLs (ie. they should start with a /)")
        }
        execute("""
        history.pushState({}, "", "$url");
        """.trimIndent())
    }

    /**
     * The absolute URL of the page, mapped to a [io.mola.galimatias.URL](http://galimatias.mola.io/apidocs/0.2.0/io/mola/galimatias/URL.html) for convenience.
     */
    val gurl : KVar<URL> = url.map(object : ReversibleFunction<String, URL>(label = "gurl") {
        override fun invoke(from: String): URL {
            return URL.parse(this@WebBrowser.httpRequestInfo.requestedUrl).resolve(from)
        }

        override fun reverse(original: String, change: URL): String {
            return change.pathQueryFragment
        }
    } )

}


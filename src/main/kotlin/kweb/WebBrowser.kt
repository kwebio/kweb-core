package kweb

import io.mola.galimatias.URL
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kweb.client.FunctionCall
import kweb.client.HttpRequestInfo
import kweb.client.Server2ClientMessage
import kweb.html.Document
import kweb.html.HtmlDocumentSupplier
import kweb.html.Window
import kweb.html.events.Event
import kweb.plugins.KwebPlugin
import kweb.state.KVar
import kweb.state.ReversibleFunction
import kweb.util.pathQueryFragment
import kweb.util.random
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * A conduit for communicating with a remote web browser, can be used to execute JavaScript and evaluate JavaScript
 * expressions and retrieve the result.
 */

private val logger = KotlinLogging.logger {}

class WebBrowser(private val sessionId: String, val httpRequestInfo: HttpRequestInfo, val kweb: Kweb) {

    private val idCounter = AtomicInteger(0)

    /**
     * During page render, the initial HTML document will be available for modification as a
     * [JSoup Document](https://jsoup.org/) in this [AtomicReference].
     *
     * Callers to [callJsFunction] may check for this being non-null, and if so edit the document
     * *instead* of some or all of the JavaScript they must call.
     *
     * The purpose of this is to implement Server-Side Rendering.
     */
    val htmlDocument = AtomicReference<org.jsoup.nodes.Document?>(null)

    fun generateId(): String = idCounter.getAndIncrement().toString(36)

    private val cachedFunctions = ConcurrentHashMap<String, Int>()

    private val plugins: Map<KClass<out KwebPlugin>, KwebPlugin> by lazy {
        HtmlDocumentSupplier.appliedPlugins.map { it::class to it }.toMap()
    }

    //TODO I think some of these things could be renamed for clarity. I think it is understandable as is, but there is room for improvement
    enum class CatcherType {
        EVENT, IMMEDIATE_EVENT, RENDER
    }
    data class OutboundMessageCatcher(var catcherType: CatcherType, val functionList: MutableList<FunctionCall>)

    /**
     * Allow us to catch outbound messages temporarily and only for this thread.  This is used for immediate
     * execution of event handlers, see `Element.onImmediate`
     */
    val outboundMessageCatcher: ThreadLocal<OutboundMessageCatcher?> = ThreadLocal.withInitial { null }

    /**
     * Are outbound messages being cached for this thread (for example, because we're inside an immediateEvent callback block)?
     */
    fun isCatchingOutbound() = outboundMessageCatcher.get()?.catcherType

    /**
     * Execute a block of code in which any JavaScript sent to the browser during the execution of the block will be stored
     * and returned by this function.
     *
     * The main use-case is recording changes made to the DOM within an onImmediate event callback so that these can be
     * replayed in the browser when an event is triggered without a server round-trip.
     */
    fun catchOutbound(catchingType: CatcherType, f: () -> Unit): List<FunctionCall> {
        require(outboundMessageCatcher.get() == null) { "Can't nest withThreadLocalOutboundMessageCatcher()" }

        val jsList = ArrayList<FunctionCall>()
        outboundMessageCatcher.set(OutboundMessageCatcher(catchingType, jsList))
        f()
        outboundMessageCatcher.set(null)
        return jsList
    }

    fun batch(catchingType: CatcherType, f: () -> Unit) {
        val caughtMessages = catchOutbound(catchingType, f)
        val server2ClientMessage = Server2ClientMessage(sessionId, caughtMessages)
        kweb.sendMessage(sessionId, server2ClientMessage)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <P : KwebPlugin> plugin(plugin: KClass<out P>): P {
        return (plugins[plugin] ?: error("Plugin $plugin is missing")) as P
    }

    /**
     * Specify that a specific plugin be provided in [Kweb.plugins], throws an exception if not.
     */
    internal fun require(vararg requiredPlugins: KClass<out KwebPlugin>) {
        val missing = HashSet<String>()
        for (requiredPlugin in requiredPlugins) {
            if (!plugins.contains(requiredPlugin)) missing.add(requiredPlugin.simpleName ?: requiredPlugin.jvmName)
        }
        if (missing.isNotEmpty()) {
            error("Plugin(s) ${missing.joinToString(separator = ", ")} required but not passed to Kweb constructor")
        }
    }

    data class FuncDeclaration(val js: String, val params: String)
    /**
     * this function substitutes "{}" in user supplied javascript, for randomly generated variable names
     */
    private fun makeJsFunction(rawJs: String): FuncDeclaration {
        val stringBuilder = StringBuilder()
        var variableCount = 1
        val params = mutableListOf<String>()
        var i = 0
        while (i < rawJs.length) {
            if (rawJs[i] == '{' && rawJs[i+1] == '}'){
                val jsVarName = "auto_var_$variableCount"
                stringBuilder.append(jsVarName)
                params.add(jsVarName)
                variableCount++
                i++//Since we matched the right bracket, we have to do an extra increment here,
            // to avoid checking the right bracket a second time.
            } else {
                stringBuilder.append(rawJs[i])
            }
            i++
        }
        return FuncDeclaration(stringBuilder.toString(), params.joinToString(separator = ","))
    }

    private fun generateCacheId() : Int {
        return abs(random.nextInt())
    }

    /**
     * Calls a JavaScript function in the browser passing it the supplied arguments, which
     * will be substituted into [jsBody] wherever a `{}` is present.
     *
     * If your JavaScript needs to use an empty JavaScript map, just insert a space
     * between the {}s, eg. `{ }`
     *
     * @sample callJsFunction_sample
     */
    fun callJsFunction(jsBody: String, vararg args: JsonElement) {
        val functionCall  = if (cachedFunctions[jsBody] != null) {
            FunctionCall(jsId = cachedFunctions[jsBody], arguments = listOf(*args))
        } else {
            val cacheId = generateCacheId()
            val func = makeJsFunction(jsBody)
            //we add the user's unmodified js as a key and the cacheId as it's value in the hashmap
            cachedFunctions[jsBody] = cacheId
            //we send the modified js to the client to be cached there.
            //we don't cache the modified js on the server, because then we'd have to modify JS on the server, everytime we want to check the server's cache
            FunctionCall(jsId = cacheId, js = func.js, parameters = func.params,
                    arguments = listOf(*args))
        }
        val debugInfo: DebugInfo? = if(!kweb.debug) null else {
            DebugInfo(jsBody, "executing", Throwable())
        }
        val outboundMessageCatcher = outboundMessageCatcher.get()
        if (outboundMessageCatcher == null) {
            kweb.callJs(sessionId, functionCall, debugInfo)
        } else {
            logger.debug("Temporarily storing message for $sessionId in threadlocal outboundMessageCatcher")
            outboundMessageCatcher.functionList.add(functionCall)
            //If we are collecting calls for an immediate event, we run the risk of the client calling JS code that has yet to be cached
            //A functionCall object having a non null js parameter, means the function has not been cached.
            //So we directly execute a line of JS code that will cache the function object, using JS generated by createCacheFunctionJs
            if (outboundMessageCatcher.catcherType == CatcherType.IMMEDIATE_EVENT) {
                if (functionCall.js != null) {
                    val cacheJs = createCacheFunctionJs(functionCall.jsId!!, functionCall.js, functionCall.parameters)
                    kweb.callJs(sessionId, FunctionCall(jsId = null, js = cacheJs), null)
                }
            }
        }
    }

    private fun callJsFunction_sample() {
        callJsFunction("alert({});", JsonPrimitive("Hello, I'm an alert box!"))
    }

    /**
     * Calls a JavaScript function that can return a value via a [callback]
     *
     * @see callJsFunction
     */
    fun callJsFunctionWithCallback(jsBody: String, callbackId: Int, callback: (JsonElement) -> Unit, vararg args: JsonElement) {
        val functionCall = if (cachedFunctions[jsBody] != null) {
            FunctionCall(jsId = cachedFunctions[jsBody], arguments = listOf(*args), callbackId = callbackId)
        } else {
            val cacheId = generateCacheId()
            val func = makeJsFunction(jsBody)
            //we add the user's unmodified js as a key and the cacheId as it's value in the hashmap
            cachedFunctions[jsBody] = cacheId
            //we send the modified js to the client to be cached there.
            //we don't cache the modified js on the server, because then we'd have to modify JS on the server, everytime we want to check the server's cache
            FunctionCall(jsId = cacheId, js = func.js, parameters = func.params,
                    arguments = listOf(*args), callbackId = callbackId)
        }
        val debugInfo: DebugInfo? = if(!kweb.debug) null else {
            DebugInfo(jsBody, "executing", Throwable())
        }
        val outboundMessageCatcher = outboundMessageCatcher.get()
        if (outboundMessageCatcher == null) {
            kweb.callJs(sessionId, functionCall, debugInfo)
        } else {
            logger.debug("Temporarily storing message for $sessionId in threadlocal outboundMessageCatcher")
            outboundMessageCatcher.functionList.add(functionCall)
            if (outboundMessageCatcher.catcherType == CatcherType.IMMEDIATE_EVENT) {
                if (functionCall.js != null) {
                    val cacheJs = createCacheFunctionJs(functionCall.jsId!!, functionCall.js, functionCall.parameters)
                    kweb.callJs(sessionId, FunctionCall(jsId = null, js = cacheJs), null)
                }
            }
        }
        kweb.addCallback(sessionId, functionCall.callbackId!!, callback)
    }

    private fun createCacheFunctionJs(cacheId: Int, functionBody: String, params: String? = null) : String {
        params?.let {
            //language=JavaScript
            return """cachedFunctions[$cacheId] = new Function("$params", "$functionBody");"""
        }
        //language=JavaScript
        return """cachedFunctions[$cacheId] = new Function("$functionBody");"""
    }

    fun removeCallback(callbackId: Int) {
        kweb.removeCallback(sessionId, callbackId)
    }

    suspend fun callJsFunctionWithResult(jsBody: String, vararg args: JsonElement): JsonElement {
        require(isCatchingOutbound() == null) {
            "You can not read the DOM inside a batched code block"
        }
        val callbackId = abs(random.nextInt())
        val cd = CompletableDeferred<JsonElement>()
        callJsFunctionWithCallback(jsBody, callbackId = callbackId, callback = { response ->
            cd.complete(response)
        }, *args)
        return cd.await()
    }

    val doc = Document(this)

    val window = Window(this)

    private data class UrlSource(val url : String, val source : Source) {
        enum class Source {
            Initial, Client, Server
        }
    }

    /*
     * We must keep track of whether a URL change was initiated on the client or on the
     * server to avoid unnecessarily pushing a new URL state back to the client when
     * that's where it originated.
     *
     * Suspect this was the cause of https://github.com/kwebio/kweb-core/issues/219
     */
    private val urlSource : KVar<UrlSource> by lazy {
        val originRelativeURL = URL.parse(httpRequestInfo.requestedUrl).pathQueryFragment
        val urlSource = KVar(UrlSource(originRelativeURL, UrlSource.Source.Initial))

        urlSource.addListener { oldState, newState ->
            logger.debug { "urlSource $oldState -> $newState" }
            if (newState.source == UrlSource.Source.Server) {
                pushState(newState.url)
            }
        }

        window.on(
            //language=JavaScript
            retrieveJs = "window.location.href"
        ).popstate { event : Event ->
            if (event.retrieved is JsonPrimitive && event.retrieved.isString) {
                urlSource.value = UrlSource(
                    URL.parse(event.retrieved.content).pathQueryFragment,
                    UrlSource.Source.Client
                )
            } else {
                error("event.retrieved isn't a string")
            }
        }

        urlSource
    }

    /**
     * The URL of the page, relative to the origin - so for the page `http://foo/bar?baz#1`, the value would be
     * `/bar?baz#1`.
     *
     * When this KVar is modified the browser will automatically update the URL in the browser along with any DOM
     * elements based on this [url] (this will be handled automatically by [kweb.routing.route]).
     *
     * If the [popstate event](https://developer.mozilla.org/en-US/docs/Web/API/Window/popstate_event) fires
     * in the browser, for example if the Back button is pressed, then this URL will also update - potentially
     * triggering re-renders of any DOM elements that depend on the URL.
     */
    val url: KVar<String>
            by lazy {
                urlSource.map(object : ReversibleFunction<UrlSource, String>("urlSource") {
                    override fun invoke(from: UrlSource) = from.url
                    override fun reverse(original: UrlSource, change: String) =
                        if (change != original.url) {
                            UrlSource(change, UrlSource.Source.Server)
                        } else {
                            UrlSource(change, original.source)
                        }
                })
            }

    private fun pushState(url: String) {
        if (!url.startsWith('/')) {
            logger.warn("pushState should only be called with origin-relative URLs (ie. they should start with a /)")
        }
        //{ } is used to initialize an empty map here. Without the space, it would be treated
        //as a variable using Kweb's template syntax
        //language=JavaScript
        callJsFunction("""
        history.pushState({ }, "", {});
        """.trimIndent(), JsonPrimitive(url))
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


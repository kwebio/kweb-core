package io.kweb.routing

import io.kweb.WebBrowser
import io.kweb.state.Observable
import io.mola.galimatias.URL
import java.net.URLDecoder


/**
 * @sample route_sample
 *
 * Created by @@jmdesprez, some modifications by @sanity
 */

// TODO: Handle window.onpopstate so that back buttons will work in a sensible way

fun main(args: Array<String>) {
    val url = URL.parse("http://a.b.c/hello?dog=cat&mouse=pig")
    println(url)
}

val urlPathTranslator = UrlPathTranslator()

fun WebBrowser.pushState(path: String) {
    execute("""history.pushState({}, "$path", "$path");""")
}

inline fun <reified T : Any> WebBrowser.route(receiver: (RequestURL<T>).() -> Unit) {
    val requestUrl = with(httpRequestInfo.requestedUrl) {
        this.query()
        RequestURL<T>(scheme = Scheme.valueOf(scheme()), host = host().toHumanString(), port = port(), path = Observable(urlPathTranslator.parse(path())), query = Observable(query()), rawUrl = this)
    }
    requestUrl.path.addListener { _, new ->
        pushState(urlPathTranslator.toPath(new) + requestUrl.query.value)
    }
    requestUrl.query.addListener { _, new ->
        pushState(urlPathTranslator.toPath(requestUrl.path.value) + new)
    }
    receiver(requestUrl)
}

data class RequestURL<T : Any>(val scheme: Scheme, val host: String, val port: Int = 80, val path: Observable<T>, val query: Observable<String>, val rawUrl: URL) {
    private fun queryToQueryMap(query: String): Map<String, String> {
        val pairs = query.split("&".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val queryMap = HashMap<String, String>()
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            queryMap.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"))
        }
        return queryMap
    }
}

enum class Scheme {
    http, https
}

fun route_sample() {
}
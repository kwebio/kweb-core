package io.kweb.routing

import io.kweb.WebBrowser
import io.kweb.state.Observable

/**
 * Created by @@jmdesprez, some modifications by @sanity
 */

// TODO: Handle window.onpopstate so that handle
// TODO: Encode and decode should occur automatically using some kind of object serialization, also need to figure out
// TODO: humanization of titles
fun <T : Any> WebBrowser.router(fromPath : (String) -> T, toPath : (T) -> String, humanize : (T) -> String = {it.toString()}) : Observable<T> {
    val decoded = fromPath(this.httpRequestInfo.requestedUrl.path())
    val observable = Observable(decoded)
    observable.addListener({ _, new ->
        doc.body.execute("""window.history.pushState({}, "${humanize(new)}", "${toPath(new)}");""")
    })
    return observable
}

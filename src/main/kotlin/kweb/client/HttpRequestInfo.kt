package kweb.client

import io.ktor.features.origin
import io.ktor.request.ApplicationRequest

/**
 * @param request This is the raw [ApplicationRequest](https://api.ktor.io/1.3.2/io.ktor.request/-application-request/index.html)
 *                provided by [Ktor](https://github.com/Kotlin/ktor), the HTTP transport library used by Kweb.  It can be used
 *                to read various information about the inbound HTTP request, however you should use properties of
 *                [HttpRequestInfo] directly instead if possible.
 *
 *                Note that values in `ApplicationRequest` can change during a connection, see [issue #154])(https://github.com/kwebio/kweb-core/issues/154).
 *
 */
data class HttpRequestInfo(val request: ApplicationRequest) {

    val requestedUrl: String by lazy {
        with(request.origin) {
            "$scheme://$host:$port$uri"
        }
    }

    val cookies = request.cookies

    val remoteHost = request.origin.remoteHost

    val userAgent = request.headers["User-Agent"]
}
package kweb.client

import io.ktor.features.origin
import io.ktor.request.ApplicationRequest

/**
 * @param request This is the raw ApplicationRequest object to [Ktor](https://github.com/Kotlin/ktor), the HTTP
 *                library used by Kweb.  It can be used to read various information about the inbound HTTP request,
 *                however you should use properties of [HttpRequestInfo] directly instead if possible.
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
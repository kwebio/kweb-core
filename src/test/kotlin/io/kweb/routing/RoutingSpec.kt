package io.kweb.routing

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import io.kotlintest.*
import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec
import io.kweb.*
import io.kweb.dom.attributes.*
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.events.on
import io.kweb.dom.element.new
import io.kweb.state.persistent.render
import mu.KotlinLogging
import java.lang.Thread.sleep

/**
 * Created by ian on 4/30/17.
 */
class RoutingSpec : FreeSpec() {

    private val logger = KotlinLogging.logger {}

    val webClient: WebClient = autoClose(ACWebClient())

    private val HTTP_PORT = 2823

    init {
        htmlUnitInit(webClient)

        val kweb = Kweb(port = HTTP_PORT) {
                route(withGalimatiasUrlParser) { url ->
                    logger.info("Rendering ${url.value}")
                    doc.body.new {
                        val path = url.path
                        render(path[0]) {
                            logger.info("Rendering path segment: '$it'")
                            when (it) {
                                ROOT_PATH -> {
                                    h1().text("Root")
                                }
                                "dogs" -> {
                                    h1(attributes = attr.id("dogHeader")).text(path[1])
                                }
                                "cats" -> {
                                    h1(attributes = attr.id("clickableHeader")).text(path.map { "${it[1]}-${it[2]}" }).on.click {
                                        path.value = listOf("dogs", "doggie")
                                    }
                                }
                                else -> {
                                    logger.warn("Unrecognized path element: $it")
                                }
                            }
                        }
                    }
                }
        }

        "Visiting /" {
            val rootPage = webClient.getPage<HtmlPage>("http://127.0.0.1:${kweb.port}/")
            rootPage.webResponse.statusCode shouldBe 200
            rootPage.getElementsByTagName("h1").let { headers ->
                headers.size shouldEqual 1
                headers.first().textContent shouldEqual "Root"
            }
        }
        /*
        "Visiting one of the dogs pages" {
            val fooJPage = webClient.getPage<HtmlPage>("http://127.0.0.1:${kweb.port}/dogs/kraken")
            fooJPage.getElementsByTagName("h1").let { headers ->
                headers.size shouldEqual 1
                "should return the appropriate header and text" {
                    headers.first().textContent shouldEqual "kraken"
                }
            }
        }
        "Visiting one of the cats pages" {
            val initialPage = webClient.getPage<HtmlPage>("http://127.0.0.1:${kweb.port}/cats/145/12")
            initialPage.getElementsByTagName("h1").let { headers ->
                headers.size shouldEqual 1
                headers.first().textContent shouldEqual "145-12"
            }

            val page = webClient.getPage<HtmlPage>("http://127.0.0.1:$HTTP_PORT/cats/145/12")
            page.getElementById("clickableHeader").let { headerElement ->
                val afterClickPage = headerElement.click<HtmlPage>()
                pollFor(5.seconds) {
                            afterClickPage.getElementById("dogHeader").textContent shouldEqual "doggie"
                }
            }
        }
        */
    }

}

val Duration.millis get() = this.timeUnit.toMillis(amount)
fun <T> pollFor(maximumTime: io.kotlintest.Duration, pollEvery : Duration = 300.milliseconds, f: () -> T): T {
    val end = System.nanoTime() + maximumTime.nanoseconds
    var times = 0
    var lastException : Throwable? = null
    while (System.nanoTime() < end) {
        try {
            val t = f()
            return t
        } catch (e: Throwable) {
            lastException = e
        }
        sleep(pollEvery.millis)
        times++
    }
    throw AssertionError("Test failed after ${maximumTime.amount} ${maximumTime.timeUnit}; attempted $times times", lastException!!)
}

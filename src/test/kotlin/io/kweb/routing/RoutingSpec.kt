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


/**
 * Created by ian on 4/30/17.
 */
class RoutingSpec : FreeSpec() {
    val webClient: WebClient = autoClose(ACWebClient())

    private val HTTP_PORT = 2826

    init {
        htmlUnitInit(webClient)

        "Given a Kweb instance serving a simple website" - {
            Kweb(port = HTTP_PORT) {
                route<FooPath> {
                    doc.body.new {
                        render(obsPath) {
                            when (it) {
                                is FooPath.Root -> {
                                    h1().text("Root")
                                }
                                is FooPath.Dogs -> {
                                    h1(attributes = attr.id("dogHeader")).text(it.j1)
                                }
                                is FooPath.Cats -> {
                                    h1(attributes = attr.id("clickableHeader")).text("${it.k1}-${it.k2}").on.click {
                                        obsPath.value = FooPath.Dogs("doggie")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Visiting /" - {
                val rootPage = webClient.getPage<HtmlPage>("http://127.0.0.1:$HTTP_PORT/")
                "should respond with a 200 code" {
                    rootPage.webResponse.statusCode shouldBe 200
                }
                rootPage.getElementsByTagName("h1").let { headers ->
                    headers.size shouldEqual 1

                    "should return the appropriate header and text" {
                        headers.first().textContent shouldEqual "Root"
                    }
                }
            }
            "Visiting one of the dogs pages" - {
                val fooJPage = webClient.getPage<HtmlPage>("http://127.0.0.1:$HTTP_PORT/dogs/kraken")
                fooJPage.getElementsByTagName("h1").let { headers ->
                    headers.size shouldEqual 1
                    "should return the appropriate header and text" {
                        headers.first().textContent shouldEqual "kraken"
                    }
                }
            }
            "Visiting one of the cats pages" - {
                "should return the appropriate text for initial pageload" {
                    val initialPage = webClient.getPage<HtmlPage>("http://127.0.0.1:$HTTP_PORT/cats/145/12")
                    initialPage.getElementsByTagName("h1").let { headers ->
                        headers.size shouldEqual 1
                        headers.first().textContent shouldEqual "145-12"
                    }
                }
                "should should return the appropriate text for a click" {
                    val page = webClient.getPage<HtmlPage>("http://127.0.0.1:$HTTP_PORT/cats/145/12")
                    page.getElementById("clickableHeader").let { headerElement ->
                        val afterClickPage = headerElement.click<HtmlPage>()
                        pollFor(5.seconds) {
                            afterClickPage.getElementById("dogHeader").textContent shouldEqual "doggie"
                        }
                    }
                }
            }
        }
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
        Thread.sleep(pollEvery.millis)
        times++
    }
    throw AssertionError("Test failed after ${maximumTime.amount} ${maximumTime.timeUnit}; attempted $times times", lastException!!)
}

sealed class FooPath {
    data class Cats(val k1: Int, val k2: Int) : FooPath()
    // localhost:port/cats/k1/k2
    data class Dogs(val j1: String) : FooPath()

    // localhost:port/dogs/abcde
    class Root : FooPath()
}
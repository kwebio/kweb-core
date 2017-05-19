package io.kweb.routing

import com.gargoylesoftware.htmlunit.AjaxController
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.WebRequest
import com.gargoylesoftware.htmlunit.html.HtmlPage
import io.kotlintest.Duration
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.milliseconds
import io.kotlintest.seconds
import io.kotlintest.specs.FreeSpec
import io.kweb.ACWebClient
import io.kweb.Kweb
import io.kweb.dom.attributes.attr
import io.kweb.dom.attributes.id
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.events.on
import io.kweb.dom.element.modification.text
import io.kweb.dom.element.new
import io.kweb.state.bind
import org.apache.commons.logging.LogFactory
import java.util.logging.Level


/**
 * Created by ian on 4/30/17.
 */
class RoutingSpec : FreeSpec() {
    val webClient: WebClient = autoClose(ACWebClient())

    init {
        webClient.ajaxController = object : AjaxController() {
            override fun processSynchron(page: HtmlPage?, request: WebRequest?, async: Boolean): Boolean {
                return true
            }
        }

        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

        "Given a Kweb instance serving a simple website" - {
            Kweb(port = 4235) {
                route<FooPath> {
                    doc.body.new {
                        bind.to(obsPath) {
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
                val rootPage = webClient.getPage<HtmlPage>("http://127.0.0.1:4235/")
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
                val fooJPage = webClient.getPage<HtmlPage>("http://127.0.0.1:4235/dogs/kraken")
                fooJPage.getElementsByTagName("h1").let { headers ->
                    headers.size shouldEqual 1
                    "should return the appropriate header and text" {
                        headers.first().textContent shouldEqual "kraken"
                    }
                }
            }
            "Visiting one of the cats pages" - {
                "should return the appropriate text for initial pageload" {
                    val initialPage = webClient.getPage<HtmlPage>("http://127.0.0.1:4235/cats/145/12")
                    initialPage.getElementsByTagName("h1").let { headers ->
                        headers.size shouldEqual 1
                        headers.first().textContent shouldEqual "145-12"
                    }
                }
                "should should return the appropriate text for a click" {
                    val initialPage = webClient.getPage<HtmlPage>("http://127.0.0.1:4235/cats/145/12")
                    initialPage.getElementById("clickableHeader").let { headerElement ->
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
fun <T> pollFor(maximumTime: io.kotlintest.Duration, pollEvery : Duration = 100.milliseconds, f: () -> T): T {
    val end = System.nanoTime() + maximumTime.nanoseconds
    var times = 0
    var lastException : Exception? = null
    while (System.nanoTime() < end) {
        try {
            return f()
        } catch (e: Exception) {
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
package io.kweb.routing

/**
 * Created by ian on 4/30/17.
 */
/*
object RoutingSpec : Spek({

    val logger = KotlinLogging.logger {}

    val webClient: WebClient = autoClose(ACWebClient())

    private val HTTP_PORT = 2823

    init {
        htmlUnitInit(webClient)

        "Given a Kweb instance" {
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

            val rootPage = webClient.getPage<HtmlPage>("http://127.0.0.1:$HTTP_PORT/")
            rootPage.webResponse.statusCode shouldBe 200
            rootPage.getElementsByTagName("h1").let { headers ->
                headers.size shouldEqual 1
                headers.first().textContent shouldEqual "Root"
            }
        }
    }
})


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
*/

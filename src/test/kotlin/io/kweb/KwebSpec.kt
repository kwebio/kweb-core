package io.kweb

import com.gargoylesoftware.htmlunit.html.HtmlPage
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new
import org.amshove.kluent.shouldEqual
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

object KwebSpec : Spek({
    given("a Kweb instance listening on port 12243") {
        on("creating a kweb server") {
            val kweb = Kweb(port = 12243) {
                doc.body.new {
                    h1().text("Lorum Ipsum")
                }
            }


            useWebClient { webClient ->


                val page = webClient.getPage<HtmlPage>("http://localhost:12243/")

                webClient.waitForBackgroundJavaScript(10000)

                page.getElementsByTagName("h1").let { h1Elements ->
                    it("should contain one H1 element containing the appropriate text") {
                        h1Elements.size shouldEqual 1
                        h1Elements.first().let { h1Element ->
                            h1Element.textContent shouldEqual "Lorum Ipsum"
                        }
                    }
                }

            }

            kweb.close()
        }

    }
})

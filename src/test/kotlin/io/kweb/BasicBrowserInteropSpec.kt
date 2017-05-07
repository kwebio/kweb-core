package io.kweb

import com.gargoylesoftware.htmlunit.AjaxController
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.WebRequest
import com.gargoylesoftware.htmlunit.html.HtmlPage
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.modification.text
import io.kweb.dom.element.new
import java.io.Closeable



/**
 * Created by ian on 4/30/17.
 */
class BasicBrowserInteropSpec : FreeSpec() {
    val webClient : WebClient = autoClose(ACWebClient().apply {
        options.apply {
            isThrowExceptionOnScriptError = false
            isThrowExceptionOnFailingStatusCode = false
        }

        // Trying to make this more robust: http://stackoverflow.com/a/5723773/16050
        ajaxController = object : AjaxController() {
            override fun processSynchron(page: HtmlPage?, request: WebRequest?, async: Boolean): Boolean {
                return true
            }
        }
    })

    init {
        "A Kweb instance generating a page with a single header" - {
            Kweb(port = 4354) {
                doc.body.new {
                    h1().text("Header")
                }
            }
            Thread.sleep(1000)
            "when a page is requested" - {
                val htmlPage = webClient.getPage<HtmlPage>("http://127.0.0.1:4354/")
                val headerElements = htmlPage.getElementsByTagName("h1")
                "a single header element should be retrieved" {
                    headerElements.size shouldBe 1
                }
                "and it should contain the text 'Header'" {
                    headerElements.first().textContent shouldBe "Header"
                }
            }
        }
    }
}

class ACWebClient : WebClient(BrowserVersion.BEST_SUPPORTED), Closeable
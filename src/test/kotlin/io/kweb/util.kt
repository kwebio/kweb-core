package io.kweb

import com.gargoylesoftware.htmlunit.*
import com.gargoylesoftware.htmlunit.html.HtmlPage
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new





/**
 * Created by ian on 6/29/17.
 */
fun useWebClient(callback: (WebClient) -> Unit) {
    WebClient(BrowserVersion.BEST_SUPPORTED).use { webClient ->
        /*
        webClient.ajaxController = object : AjaxController() {
            override fun processSynchron(page: HtmlPage?, request: WebRequest?, async: Boolean): Boolean {
                return true
            }
        }
        */
        webClient.ajaxController = object : AjaxController() {
            override fun processSynchron(page: HtmlPage?, request: WebRequest?, async: Boolean): Boolean {
                return true
            }
        }
/*
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog")
        Logger.getLogger("com.gargoylesoftware").level = Level.OFF
        Logger.getLogger("org.apache.commons.httpclient").level = Level.OFF
*/

        callback(webClient)
    }
}

fun main(args: Array<String>) {
    val kweb = Kweb(port = 1251) {
        doc.body.new {
            h1().text("Hello")
        }
    }

    useWebClient { wc ->
        val google = wc.getPage<HtmlPage>("http://google.com/")
        println(google.titleText)
    }

    kweb.close()
}
package io.kweb

import com.gargoylesoftware.htmlunit.AjaxController
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.WebRequest
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.apache.commons.logging.LogFactory
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by ian on 6/29/17.
 */
fun htmlUnitInit(webClient: WebClient) {
    webClient.ajaxController = object : AjaxController() {
        override fun processSynchron(page: HtmlPage?, request: WebRequest?, async: Boolean): Boolean {
            return true
        }
    }

    LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog")
    Logger.getLogger("com.gargoylesoftware").level = Level.OFF
    Logger.getLogger("org.apache.commons.httpclient").level = Level.OFF
}
package kweb.demos.https

import kweb.Kweb
import kweb.div
import kweb.h1
import kweb.https.SSLConfig
import kweb.new
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin

fun main() {
    Kweb(9090, plugins = listOf(fomanticUIPlugin), httpsConfig = SSLConfig()) {
        doc.body.new {
            div(fomantic.ui.main.container).new {
                div(fomantic.column).new {
                    div(fomantic.ui.vertical.segment).new {
                        div(fomantic.ui.message).new {
                            h1(fomantic.ui.header.center.aligned).text("Welcome to Https Kweb!")
                        }
                    }
                }
            }
        }
    }
}
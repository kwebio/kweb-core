package kweb.demos.https

import kweb.Kweb
import kweb.dom.element.creation.tags.div
import kweb.dom.element.creation.tags.h1
import kweb.dom.element.new
import kweb.https.SSLConfig
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
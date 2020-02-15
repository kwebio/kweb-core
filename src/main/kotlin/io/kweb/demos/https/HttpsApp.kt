package io.kweb.demos.https

import io.kweb.Kweb
import io.kweb.dom.element.creation.tags.div
import io.kweb.dom.element.creation.tags.h1
import io.kweb.dom.element.new
import io.kweb.https.SSLConfig
import io.kweb.plugins.fomanticUI.fomantic
import io.kweb.plugins.fomanticUI.fomanticUIPlugin

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
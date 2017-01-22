package com.github.sanity.kweb.demos.materialDesignLite

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.element.creation.a
import com.github.sanity.kweb.dom.element.creation.h1
import com.github.sanity.kweb.dom.element.events.on
import com.github.sanity.kweb.dom.element.modification.setText
import com.github.sanity.kweb.plugins.materialdesignlite.layout.layout
import com.github.sanity.kweb.plugins.materialdesignlite.layout.miniFooter
import com.github.sanity.kweb.plugins.materialdesignlite.layout.tabs
import com.github.sanity.kweb.plugins.materialdesignlite.materialDesignLite
import com.github.sanity.kweb.plugins.materialdesignlite.mdl
import com.github.sanity.kweb.plugins.materialdesignlite.typography.TypographyStyles
import com.github.sanity.kweb.plugins.materialdesignlite.typography.typography

fun main(args: Array<String>) {
    println("Visit http://127.0.0.1:8091/ in your browser...")
    KWeb(port = 8091, plugins = listOf(materialDesignLite)) {
        doc.body.apply {
            mdl.layout(fixedHeader = true).apply {
                header().apply {
                    row().apply {
                        title().setText("Title")
                        spacer()
                        navigation().apply {
                            navLink().setText("Link")
                            navLink().setText("Link")
                            navLink().setText("Link")
                            navLink().setText("Link")
                        }
                    }
                }
                drawer().apply {
                    title().setText("Title")
                    navigation().apply {
                        navLink().setText("Link")
                        navLink().setText("Link")
                        navLink().setText("Link")
                        navLink().setText("Link")
                    }
                }
                content().apply {
                    tabs().apply {
                        panel("One", isActive = true).apply {
                            h1().typography(TypographyStyles.title).setText("This is panel One")
                        }
                        panel("Two").apply {
                            h1().typography(TypographyStyles.title).setText("This is panel Two")
                        }
                        panel("Three").apply {
                            h1().typography(TypographyStyles.title).setText("This is panel Three")
                        }
                    }
                }
                mdl.miniFooter().apply {
                    leftSection().apply {
                        logo()
                        linkList().apply {
                            li().a().setText("Footer link")
                                    .on.click {
                                println("Footer link clicked")
                            }
                            li().a().setText("Footer link")
                            li().a().setText("Footer link")
                            li().a().setText("Footer link")
                        }
                    }
                    rightSection().apply {
                        linkList().apply {
                            li().a().setText("Footer link")
                            li().a().setText("Footer link")
                            li().a().setText("Footer link")
                            li().a().setText("Footer link")
                        }
                    }
                }
            }
        }
    }
}
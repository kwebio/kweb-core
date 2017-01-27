package com.github.sanity.kweb.demos.materialDesignLite

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.element.creation.a
import com.github.sanity.kweb.dom.element.creation.span
import com.github.sanity.kweb.dom.element.events.on
import com.github.sanity.kweb.dom.element.modification.setText
import com.github.sanity.kweb.plugins.materialdesignlite.layout.*
import com.github.sanity.kweb.plugins.materialdesignlite.list.ListItemType
import com.github.sanity.kweb.plugins.materialdesignlite.list.MDLUlElement
import com.github.sanity.kweb.plugins.materialdesignlite.list.list
import com.github.sanity.kweb.plugins.materialdesignlite.materialDesignLite
import com.github.sanity.kweb.plugins.materialdesignlite.mdl
import com.github.sanity.kweb.plugins.materialdesignlite.table.MDLTableHeaderName
import com.github.sanity.kweb.plugins.materialdesignlite.table.table

fun main(args: Array<String>) {
    println("Visit http://127.0.0.1:8091/ in your browser...")
    KWeb(port = 8091, plugins = listOf(materialDesignLite)) {
        doc.body.apply {
            mdl.layout(fixedHeader = true).apply {
                addHeader()
                addDrawer()
                addContent()
                addFooter()
            }
        }
    }
}

private fun LayoutElement.addHeader() {
    header().apply {
        row().apply {
            title().setText("Title")
            spacer()
            navigation().apply {
                navLink().setText("First header link")
                navLink().setText("Second header link")
                navLink().setText("Third header link")
                navLink().setText("Fourth header link")
            }
        }
    }
}

private fun LayoutElement.addDrawer() {
    drawer().apply {
        title().setText("Title")
        navigation().apply {
            navLink().setText("First drawer link")
            navLink().setText("Second drawer link")
            navLink().setText("Third drawer link")
            navLink().setText("Fourth drawer link")
        }
    }
}

private fun LayoutElement.addContent() {
    content().apply {
        tabs().apply {
            addTableFromObjectsPanel()
            addManualTablePanel()
            addListPanel()
        }
    }
}

private fun TabsElement.addTableFromObjectsPanel() {
    panel("Table from Objects", isActive = true).apply {
        grid().div(offsetBefore = 5, width = 2).apply {
            table().apply {
                data class Row(@MDLTableHeaderName("First Name") val firstName: String, val address: String)

                val data = listOf(Row("Ian", "Austin"), Row("Fred", "Houston"))
                fromDataObjectList(data, propertyOrder = listOf(Row::firstName, Row::address)) { obj, td ->
                    td.on.click {
                        println(obj.firstName)
                    }
                }
            }
        }
    }
}

private fun TabsElement.addManualTablePanel() {
    panel("Manual table").apply {
        grid().div(offsetBefore = 5, width = 2).apply {
            table().apply {
                thead().apply {
                    tr().apply {
                        th().setText("First")
                        th().setText("Second")
                        th().setText("Third")
                    }
                }
                tbody().apply {
                    tr().apply {
                        td().setText("dog")
                        td().setText("cat")
                        td().setText("mouse")
                    }
                    tr().apply {
                        td().setText("egg")
                        td().setText("hen")
                        td().setText("cat")
                    }
                }
            }
        }
    }
}

private fun TabsElement.addListPanel() {
    panel("List").apply {
        grid().div(offsetBefore = 2, width = 8).apply {
            list().apply {
                addActorItem("Bryan Cranston", "Bryan Cranston played the role of Walter in Breaking Bad. He is also known for playing Hal in Malcom in the Middle.")
                addActorItem("Aaron Paul", "Aaron Paul played the role of Jesse in Breaking Bad. He also featured in the \"Need For Speed\" Movie.")
                addActorItem("Bob Odenkirk", "Bob Odinkrik played the role of Saul in Breaking Bad. Due to public fondness for the character, Bob stars in his own show now, called \"Better Call Saul\".")
            }
        }
    }
}

private fun MDLUlElement.addActorItem(name: String, bio: String) {
    item(type = ListItemType.threeLine).apply {
        primaryContent().apply {
            avatar().setText("person")
            span().setText(name)
            body().setText(bio)
        }
        secondaryContent().apply {
            secondaryAction().icon().setText("star")
        }
    }
}

private fun LayoutElement.addFooter() {
    mdl.miniFooter().apply {
        addLeftSection()
        addRightSection()
    }
}

private fun MiniFooterElement.addLeftSection() {
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
}

private fun MiniFooterElement.addRightSection() {
    rightSection().apply {
        linkList().apply {
            li().a().setText("Footer link")
            li().a().setText("Footer link")
            li().a().setText("Footer link")
            li().a().setText("Footer link")
        }
    }
}



package com.github.sanity.kweb.demos.materialDesignLite

import com.github.sanity.kweb.Kweb
import com.github.sanity.kweb.dom.element.creation.insert
import com.github.sanity.kweb.dom.element.events.on
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
    Kweb(port = 8091, plugins = listOf(materialDesignLite)) {
        doc.body.apply {
            create().mdl.layout(fixedHeader = true).apply {
                addHeader()
                addDrawer()
                addContent()
                addFooter()
            }
        }
    }
}

private fun LayoutCreator.addHeader() {
    header().apply {
        row().apply {
            title().text("Title")
            spacer()
            navigation().apply {
                navLink().text("First header link")
                navLink().text("Second header link")
                navLink().text("Third header link")
                navLink().text("Fourth header link")
            }
        }
    }
}

private fun LayoutCreator.addDrawer() {
    drawer().apply {
        title().text("Title")
        navigation().apply {
            navLink().text("First drawer link")
            navLink().text("Second drawer link")
            navLink().text("Third drawer link")
            navLink().text("Fourth drawer link")
        }
    }
}

private fun LayoutCreator.addContent() {
    content().create().mdl.apply {
        tabs().apply {
            addTableFromObjectsPanel()
            addManualTablePanel()
            addListPanel()
        }
    }
}

private fun TabsElement.addTableFromObjectsPanel() {
    panel("Table from Objects", isActive = true).apply {
        grid().cell(offsetBefore = 5, width = 2).apply {
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
        grid().cell(offsetBefore = 5, width = 2).apply {
            table().apply {
                thead().apply {
                    tr().apply {
                        th().text("First")
                        th().text("Second")
                        th().text("Third")
                    }
                }
                tbody().apply {
                    tr().apply {
                        td().text("dog")
                        td().text("cat")
                        td().text("mouse")
                    }
                    tr().apply {
                        td().text("egg")
                        td().text("hen")
                        td().text("cat")
                    }
                }
            }
        }
    }
}

private fun TabsElement.addListPanel() {
    panel("List").apply {
        grid().cell(offsetBefore = 2, width = 8).apply {
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
            avatar().text("person")
            span().text(name)
            body().text(bio)
        }
        secondaryContent().apply {
            secondaryAction().icon().text("star")
        }
    }
}

private fun LayoutCreator.addFooter() {
    miniFooter().apply {
        addLeftSection()
        addRightSection()
    }
}

private fun MiniFooterCreator.addLeftSection() {
    leftSection().apply {
        logo()
        linkList().apply {
            li().a().text("Footer link")
                    .on.click {
                println("Footer link clicked")
            }
            li().a().text("Footer link")
            li().a().text("Footer link")
            li().a().text("Footer link")
        }
    }
}

private fun MiniFooterCreator.addRightSection() {
    rightSection().apply {
        linkList().apply {
            li().a().text("Footer link")
            li().a().text("Footer link")
            li().a().text("Footer link")
            li().a().text("Footer link")
        }
    }
}



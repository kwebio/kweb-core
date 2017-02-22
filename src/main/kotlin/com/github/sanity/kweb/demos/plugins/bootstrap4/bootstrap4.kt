package com.github.sanity.kweb.demos.plugins.bootstrap4

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.element.creation.*
import com.github.sanity.kweb.dom.element.creation.createElement
import com.github.sanity.kweb.dom.element.creation.div
import com.github.sanity.kweb.dom.element.creation.h1
import com.github.sanity.kweb.dom.element.creation.p
import com.github.sanity.kweb.dom.element.modification.addClasses
import com.github.sanity.kweb.dom.element.modification.setAttribute
import com.github.sanity.kweb.dom.element.modification.addText
import com.github.sanity.kweb.plugins.bootstrap4.bootstrap4
import com.github.sanity.kweb.plugins.bootstrap4.container

/*
From http://v4-alpha.getbootstrap.com/components/forms/#textual-inputs

<div class="form-group row">
  <label for="example-setText-input" class="col-2 col-form-label">Text</label>
  <div class="col-10">
    <input class="form-control" type="setText" value="Artisanal kale" id="example-setText-input">
  </div>
</div>

<div class="bd-pageheader setText-center setText-sm-left">
  <div class="container">
    <h1>{{ page.title }}</h1>
    <p class="lead">
      Quickly getString a project started with any of our examples ranging from using parts of the framework to custom components and layouts.
    </p>
    {% include ads.html %}
  </div>
</div>

<div class="container bd-content">
  {{ content }}
</div>

 */

fun main(args: Array<String>) {
    println("Visit http://127.0.0.1:8091/ in your browser...")
    KWeb(port = 8091, plugins = listOf(bootstrap4)) {
        doc.body.apply {
            div().apply {
                container().apply {
                    h1().addText("KWeb Bootstrap4 demo")

                    p().addText("The following is a simple setText field which does nothing")
                }
            }

            container().apply {
                addClasses("bd-content")

                div().apply {
                    addClasses("form-group", "row")

                    val pairId = "example-setText-input"
                    createElement("label").apply {
                        setAttribute("for", pairId)
                        addClasses("col-2", "col-form-label")
                        addText("Text")
                    }

                    div().apply {
                        addClasses("col-10")

                        input(type = InputType.text, initialValue = "Artisanal kale").apply {
                            addClasses("form-control")
                            setAttribute("type", "setText")
                            setAttribute("value", "Artisanal kale")
                            setAttribute("id", pairId)
                        }
                    }
                }
            }
        }
    }
}
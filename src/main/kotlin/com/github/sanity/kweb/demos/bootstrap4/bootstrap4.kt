package com.github.sanity.kweb.demos.bootstrap4

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.Element.InputType.text
import com.github.sanity.kweb.plugins.bootstrap4.bootstrap4

/*
From http://v4-alpha.getbootstrap.com/components/forms/#textual-inputs

<div class="form-group row">
  <label for="example-text-input" class="col-2 col-form-label">Text</label>
  <div class="col-10">
    <input class="form-control" type="text" value="Artisanal kale" id="example-text-input">
  </div>
</div>

<div class="bd-pageheader text-center text-sm-left">
  <div class="container">
    <h1>{{ page.title }}</h1>
    <p class="lead">
      Quickly get a project started with any of our examples ranging from using parts of the framework to custom components and layouts.
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
                addClasses("bd-pageheader", "text-center", "text-sm-left")

                div().apply {
                    setClasses("container")

                    h1("KWeb Bootstrap4 demo")

                    p("The following is a simple text field which does nothing")
                }
            }

            div().apply {
                addClasses("container", "bd-content")

                div().apply {
                    addClasses("form-group", "row")

                    val pairId = "example-text-input"
                    createElement("label").apply {
                        setAttribute("for", pairId)
                        addClasses("col-2", "col-form-label")
                        text("Text")
                    }

                    div().apply {
                        addClasses("col-10")

                        input(type = text, initialValue = "Artisanal kale").apply {
                            addClasses("form-control")
                            setAttribute("type", "text")
                            setAttribute("value", "Artisanal kale")
                            setAttribute("id", pairId)
                        }
                    }
                }
            }
        }
    }
}
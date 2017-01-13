package com.github.sanity.kweb.demos.bootstrap4

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.plugins.bootstrap4.bootstrap4

/*
From http://v4-alpha.getbootstrap.com/components/forms/#textual-inputs

<div class="form-group row">
  <label for="example-text-input" class="col-2 col-form-label">Text</label>
  <div class="col-10">
    <input class="form-control" type="text" value="Artisanal kale" id="example-text-input">
  </div>
</div>
 */

fun main(args: Array<String>) {
    println("Visit http://127.0.0.1:8091/ in your browser...")
    KWeb(port = 8091, plugins = listOf(bootstrap4)) {
        doc.body.apply {
            createElement("div").apply {
                setAttribute("class", "form-group row")

                val pairId = "example-text-input"
                createElement("label").apply {
                    setAttribute("for", pairId)
                    setAttribute("class", "col-2 col-form-label")
                    text("Text")
                }

                createElement("div").apply {
                    setAttribute("class", "col-10")

                    createElement("input").apply {
                        setAttribute("class", "form-control")
                        setAttribute("type", "text")
                        setAttribute("value", "Artisanal kale")
                        setAttribute("id", pairId)
                    }
                }
            }
        }
    }
}
package com.github.sanity.kweb.demos.bootstrap4

import com.github.sanity.kweb.KWeb
import com.github.sanity.kweb.dom.element.creation.h1
import com.github.sanity.kweb.dom.element.modification.setText
import com.github.sanity.kweb.plugins.materialdesignlite.TypographyStyles.display3
import com.github.sanity.kweb.plugins.materialdesignlite.materialDesignLite
import com.github.sanity.kweb.plugins.materialdesignlite.mdl
import com.github.sanity.kweb.plugins.materialdesignlite.typography

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
    KWeb(port = 8091, plugins = listOf(materialDesignLite)) {
        doc.body.apply {
            mdl.drawerLayout().apply {
                drawer().apply {
                    title().setText("KWeb")
                    nav().apply {
                        link().setText("One")
                        link().setText("Two")
                        link().setText("Three")
                    }
                }
                content().apply {
                    h1().typography(display3).setText("This is the page content")
                }
            }
        }
    }
}
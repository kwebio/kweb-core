package com.github.sanity.kweb

import com.moodysalem.phantomjs.wrapper.PhantomJS
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.concurrent.thread

/**
 * Created by ian on 1/10/17.
 */
class PhantomJSSpec : FreeSpec() {
    init {
        "Should be able to set and retrieve an attribute" {
            thread {
                KWeb(7324) {
                    doc.body.apply {
                        async {
                            val h1 = h1("testing").class_("testclass")
                            println("Read class: ${h1.read.class_.await()}")
                        }.get()
                    }
                }
            }
            Thread.sleep(500)
            val script =
                    """
var page = require('webpage').create();
page.open('http://127.0.0.1:7324', function(status) {
  console.log("Status: " + status);
  if(status === "success") {
    page.render('example.png');
  }
  phantom.exit();
});
                """.trimIndent()
            PhantomJS.exec(script.byteInputStream())
        }
    }
}
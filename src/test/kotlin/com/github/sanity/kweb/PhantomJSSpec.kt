package com.github.sanity.kweb

import com.moodysalem.phantomjs.wrapper.PhantomJS
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import java.util.concurrent.CompletableFuture

/**
 * Created by ian on 1/10/17.
 */
class PhantomJSSpec : FreeSpec() {
    init {
        "Should be able to set and retrieve an attribute" {
                KWeb(7324) {
                    println("Hello")
                    with(doc.body) {
                        println("Hello2")
                        val future: CompletableFuture<Unit> = async {
                            //                            throw RuntimeException("Fake exception")
                            println("Writing H1")
                            val h1 = h1("testing").setAttribute("data-test", "abacus")
                            println("H1 written")
                            println("Read " + h1.read.attribute("data-test").await())
                            println("after read")
                            execute("console.log('Kweb works');")
                        }
                        // The function is delayed, ties up the receiver thread in KWeb.handleWS()
                        Thread.sleep(10000)
                        /*  try {
                          future.get()  // this seems to prevent .await() from working
                      } catch (e : ExecutionException) {
                          throw e.cause!!
                      }*/
                    }
                }

            val script =
                    """
phantom.onError = function(msg, trace) {
  var msgStack = ['PHANTOM ERROR: ' + msg];
  if (trace && trace.length) {
    msgStack.push('TRACE:');
    trace.forEach(function(t) {
      msgStack.push(' -> ' + (t.file || t.sourceURL) + ': ' + t.line + (t.function ? ' (in function ' + t.function +')' : ''));
    });
  }
  console.error(msgStack.join('\n'));
  phantom.exit(1);
};
var page = require('webpage').create();
page.onConsoleMessage = function(msg, lineNum, sourceId) {
  console.log('CONSOLE: ' + msg);
};
page.includeJs("http://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.min.js", function() {
  page.open('http://127.0.0.1:7324', function(status) {
    if (status === "success") {
       window.setTimeout(function () {
            phantom.exit();
        }, 15000);
      //waitFor("$('.testclass').is(':visible')", "console.log('<h1> rendered');");
    }
  })
});

                """.trimIndent()
            val result = PhantomJS.exec(script.byteInputStream())
            println(result.stdOut)

        }

    }
}
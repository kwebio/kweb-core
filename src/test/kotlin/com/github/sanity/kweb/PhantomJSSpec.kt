package com.github.sanity.kweb

import com.moodysalem.phantomjs.wrapper.PhantomJS
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

/**
 * This works, but is very limited and clunky.  Really need a way to test all of the JavaScript stuff.
 */
class PhantomJSSpec : FreeSpec() {
    init {
        val exceptionFuture = CompletableFuture<Unit>()

        "should be able to set and read an attribute" {
            KWeb(7324) {
                    with(doc.body) {
                        async {
                            val h1 = h1("testing").setAttribute("data-test", "abacus")
                            h1.read.attribute("data-test").await() shouldBe "abacus"
                            exceptionFuture.complete(Unit)
                        }.exceptionally { exceptionFuture.completeExceptionally(it) }
                    }
            }
            val script = String(Files.readAllBytes(Paths.get(javaClass.getResource("phantomInit.js").toURI())), StandardCharsets.UTF_8)
            thread {
                // Launch phantom web server
                val result = PhantomJS.exec(script.byteInputStream())
            }
            exceptionFuture.get()
        }

    }
}


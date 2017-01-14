package com.github.sanity.kweb

import com.github.sanity.kweb.dom.element.creation.attr
import com.github.sanity.kweb.dom.element.creation.classes
import com.github.sanity.kweb.dom.element.creation.h1
import com.github.sanity.kweb.dom.element.creation.set
import com.github.sanity.kweb.dom.element.read.read
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

        /* TODO: Find a way to split this up
         * Roman Elizarov [JB] [2:42 AM]
            @ianclarke To simplify this things I'll introduce `blockingRun { ... }` primitive that starts coroutine, but blocks until it returns.

            [2:44]
            You should be able to just do `async { ... my code that I want to test ... }.await()` in your tests.

            [2:44]
            It basically has the same semantics.

            [2:45]
            Your case is more complicated, though

            [2:47]
            I think you need to do `join()` on the thread you're launching your `PhantomJS.exec` in

            [2:48]
            You need to define some helper function for tests that starts a separate thread with your engine and waits until its done. Or don't launch a separate thread at all, but do everything in the main test thread.
         */
        "test various server-browser interactions" {
            KWeb(7324) {
                    with(doc.body) {
                        async {
                            val h1 = h1(attributes = attr.classes("testing").set("data-test", "abacus"))
                            h1.read.attribute("data-test").await() shouldBe "abacus"

                            doc.cookie.setString("testcookie", "hello")
                            doc.cookie.getString("testcookie").await() shouldBe "hello"
                            doc.cookie.remove("testcookie")
                            doc.cookie.getString("testcookie").await() shouldBe null

                            doc.localStorage.set("objtest", 123)
                            doc.localStorage.get<Int>("objtest").await() shouldBe 123
                            doc.localStorage.remove("objtest")
                            doc.localStorage.getString("objtest").await() shouldBe null

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


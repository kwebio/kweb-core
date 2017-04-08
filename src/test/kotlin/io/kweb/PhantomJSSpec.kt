package io.kweb

import io.kotlintest.specs.FreeSpec

/**
 * This works, but is very limited and clunky.  Really need a way to test all of the JavaScript stuff.
 */
class PhantomJSSpec : FreeSpec() {
    init {
        /*
        val exceptionFuture = CompletableFuture<Unit>()

        // TODO: Just execute all demos and make sure there are no javascript errors

         TODO: Find a way to split this up
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

        "test various server-browser interactions" {
            KWeb(7324) {
                    val cookie = doc.cookie
                    with(doc.body) {
                        val localStorage = localStorage
                        future {
                            val h1 = h1(attributes = attr.classes("testing").set("data-test", "abacus"))
                            h1.read.attribute("data-test").await() shouldBe "abacus"

                            cookie.setString("testcookie", "hello")
                            cookie.getString("testcookie").await() shouldBe "hello"
                            cookie.remove("testcookie")
                            cookie.getString("testcookie").await() shouldBe null

                            localStorage.set("objtest", 123)
                            localStorage.get<Int>("objtest").await() shouldBe 123
                            localStorage.remove("objtest")
                            localStorage.getString("objtest").await() shouldBe null

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
*/
    }
}


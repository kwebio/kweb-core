package kweb.client

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ClientConnectionTest : FunSpec({
  val caching = ClientConnection.Caching("test")

  test("Adding elements to the queue") {
    caching.send("Message 1")
    caching.send("Message 2")
    caching.queueSize() shouldBe 2
  }

  test("Reading elements from the queue") {
    val readList = caching.read()
    readList.size shouldBe 2
    readList[0] shouldBe "Message 1"
    readList[1] shouldBe "Message 2"
  }

  test("Queue should be empty after read") {
    caching.queueSize() shouldBe 0
  }

  test("Queue cannot be written to after it has been read") {
    shouldThrow<IllegalStateException> {
      caching.send("Message 3")
    }
  }

  test("Queue can only be read once") {
    shouldThrow<IllegalStateException> {
      caching.read()
    }
  }
})

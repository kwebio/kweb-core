<<<<<<< HEAD
package io.kweb.state

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

object KVarSpec : StringSpec({
    "a KVar with value `dog`" {
        data class Foo(val bar : String)
        val f = Foo("dog")
        val kvf = KVar(f)
        "should have field with value `dog`" {
            kvf.value.bar shouldBe "dog"
        }

        val kvfp = kvf.property(Foo::bar)

        kvfp.value = "cat"

        "should have modified the underlying KVar" {
            kvf.value.bar shouldBe "cat"
        }
    }
})
=======
package io.kweb.state

import org.amshove.kluent.`should be equal to`
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object KVarSpec : Spek({
    describe("a KVar with value `dog`") {
        data class Foo(val bar : String)
        val f = Foo("dog")
        val kvf = KVar(f)
        it("should have field with value `dog`") {
            kvf.value.bar `should be equal to` "dog"
        }

        val kvfp = kvf.property(Foo::bar)

        kvfp.value = "cat"

        it ("should have modified the underlying KVar") {
            kvf.value.bar `should be equal to` "cat"
        }
    }
})
>>>>>>> jupiter-tests

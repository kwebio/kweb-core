package kweb.state

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class KVarSpec : FreeSpec({
    "a KVar with value `dog`" - {
        data class Foo(val bar: String)

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

    "a mapped kvar" - {
        "should work bidirectionally" {
            val kv = KVar("one")
            kv.value = "three"
            val mappedKv = kv.map { it.length }
            mappedKv.value shouldBe 5
            kv.value = "one"
            mappedKv.value shouldBe 3
        }
    }

    "a Pair of Kvars" - {
        val kvarPair = KVar(1) to KVar(2)
        "should be convertible to a single KVar" {
            val newKV = kvarPair.combine()
            newKV.value shouldBe (1 to 2)
            kvarPair.first.value = 5
            kvarPair.second.value = 6
            newKV.value shouldBe (5 to 6)
            newKV.value = (9 to 10)
            kvarPair.first.value shouldBe 9
            kvarPair.second.value shouldBe 10
        }
    }

    "Two KVar<List>s can be combined" - {
        val kv1 = KVar(listOf(1, 2, 3))
        val kv2 = KVar(listOf(4, 5, 6))
        val combined = kv1 + kv2
        "should have the correct initial value" {
            combined.value shouldBe listOf(1, 2, 3, 4, 5, 6)
        }
        "should update correctly when the first KVar is updated" {
            kv1.value = listOf(7, 8, 9)
            combined.value shouldBe listOf(7, 8, 9, 4, 5, 6)
        }
        "should update correctly when the second KVar is updated" {
            kv2.value = listOf(10, 11, 12)
            combined.value shouldBe listOf(7, 8, 9, 10, 11, 12)
        }
    }
})

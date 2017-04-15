package io.kweb

import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 4/15/17.
 */
class MiscSpec : FreeSpec() {
    init {
        "test .pkg" {
            MiscSpec::class.pkg shouldBe "io.kweb"
        }
    }
}
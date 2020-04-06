// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class ExtensionTest : ShouldSpec({
    should("abbreviate strings correctly") {
        "Quack quack".abbreviate(6) shouldBe "Quack…"
        "Quack Quack".abbreviate(80) shouldBe "Quack Quack"
        shouldThrow<IllegalArgumentException> { "Quack".abbreviate(1) }
    }

    should("center strings correctly") {
        "Quack".center(3, '-') shouldBe "Quack"
        "Quack".center(9, '-') shouldBe "--Quack--"
    }

    should("wrap strings correctly") {
        "This text should be wrapped".wrap(10) shouldBe "This text\nshould be\nwrapped"
    }
})

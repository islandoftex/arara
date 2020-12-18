// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.islandoftex.arara.api.AraraException

class InputHandlingTest : ShouldSpec({
    should("return truthy values for common truthy input") {
        listOf("yes", "true", "1", "on", "Yes", "True", "On", "YES", "TRUE",
                "ON", "YeS", "yEs", "TrUe", "oN").forAll {
            InputHandling.checkBoolean(it) shouldBe true
        }
    }
    should("return falsy values for common falsy input") {
        listOf("no", "false", "0", "off", "No", "False", "Off", "NO", "FALSE",
                "OFF", "nO", "oFf", "fAlsE").forAll {
            InputHandling.checkBoolean(it) shouldBe false
        }
    }
    should("throw on ambiguous boolean input") {
        listOf("quack", "duck", "yess", "yyes", "noo", "nno").forAll {
            shouldThrow<AraraException> {
                InputHandling.checkBoolean(it)
            }.message shouldContain "not a valid boolean"
        }
    }

    should("flatten lists correctly") {
        InputHandling.flatten(listOf(1, 2, listOf(3, 4, listOf(5, 6))))
                .toSet() shouldBe (setOf(1, 2, 3, 4, 5, 6) as Set<Any>)
    }
})

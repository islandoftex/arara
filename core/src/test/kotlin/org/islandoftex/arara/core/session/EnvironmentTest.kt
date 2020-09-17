// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class EnvironmentTest : ShouldSpec({
    should("return null on non-existent system variable") {
        Environment.getSystemPropertyOrNull("asdf") shouldBe null
    }
    should("hold a fallback value on non-existent system variable") {
        Environment.getSystemProperty("asdf", "fallback") shouldBe "fallback"
    }

    should("have a non-null value for existent variable") {
        listOf("/", "\\") shouldContain Environment.getSystemPropertyOrNull("file.separator")
    }
    should("have a non-fallback value for existent variable") {
        listOf("/", "\\") shouldContain Environment.getSystemProperty("file.separator", "fallback")
    }
})

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class SystemCallUtilsTest : ShouldSpec({
    should("find system utils") {
        SystemCallUtils.isOnPath("echo") shouldBe true
    }
    should("not find utils with fantasy name") {
        // hopefully no one will have such a command in the path…
        SystemCallUtils.isOnPath("echoQuackForArara") shouldBe false
    }
})

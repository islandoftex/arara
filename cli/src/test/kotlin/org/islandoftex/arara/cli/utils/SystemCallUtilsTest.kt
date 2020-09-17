// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.islandoftex.arara.core.system.OSCompatibilityLayer

class SystemCallUtilsTest : ShouldSpec({
    should("find system utils") {
        OSCompatibilityLayer.isOnPath("echo") shouldBe true
    }
    should("not find utils with fantasy name") {
        // hopefully no one will have such a command in the path…
        OSCompatibilityLayer.isOnPath("echoQuackForArara") shouldBe false
    }
})

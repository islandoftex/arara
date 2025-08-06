// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.time

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.time.ZoneId

class DateTimeTest : ShouldSpec({
    should("correctly parse date to string") {
        DateTime(2025, 8, 1, 17, 46, 1)
                .toString() shouldBe "2025-08-01 17:46:01"
    }

    should("correctly parse date to long") {
        DateTime(2025, 8, 1, 3, 9, 11)
                .toLong(ZoneId.of("UTC")) shouldBe 1754017751000
    }

    should("correctly use system's default timezone") {
        DateTime(2025, 8, 1, 3, 9, 11)
                .toLong() shouldBe DateTime(2025, 8, 1, 3, 9, 11)
                .toLong(ZoneId.systemDefault())
    }

    should("correctly get every date component") {
        with (DateTime(2025, 8, 1, 3, 9, 11)) {
            year shouldBe 2025
            month shouldBe 8
            day shouldBe 1
            hour shouldBe 3
            minute shouldBe 9
            second shouldBe 11
        }
    }
})

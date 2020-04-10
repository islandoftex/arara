// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.islandoftex.arara.Arara
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.localization.Language

class CommonUtilsTest : ShouldSpec({
    should("format bytes correctly") {
        Arara.config[AraraSpec.Execution.language] = Language("en")
        mapOf(800 to "800 B",
                1000 to "1.0 kB",
                1024 to "1.0 kB",
                1000000 to "1.0 MB").forEach { (key, value) ->
            CommonUtils.byteSizeToString(key.toLong()) shouldBe value
        }
    }

    should("flatten lists correctly") {
        CommonUtils.flatten(listOf(1, 2, listOf(3, 4, listOf(5, 6))))
                .toSet() shouldBe (setOf(1, 2, 3, 4, 5, 6) as Set<Any>)
    }
})

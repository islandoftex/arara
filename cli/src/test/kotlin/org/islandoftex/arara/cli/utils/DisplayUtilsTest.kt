// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import java.util.Locale
import org.islandoftex.arara.Arara
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.core.configuration.UserInterfaceOptions

class DisplayUtilsTest : ShouldSpec({
    should("format bytes correctly") {
        Arara.config[AraraSpec.userInterfaceOptions] =
                UserInterfaceOptions(Locale("en"))
        mapOf(800 to "800 B",
                1000 to "1.0 kB",
                1024 to "1.0 kB",
                1000000 to "1.0 MB").forEach { (key, value) ->
            DisplayUtils.byteSizeToString(key.toLong()) shouldBe value
        }
    }
})

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.session.Session

class DisplayUtilsTest : ShouldSpec({
    should("format bytes correctly") {
        Session.userInterfaceOptions = UserInterfaceOptions(MPPLocale("en"))
        mapOf(800 to "800 B",
                1000 to "1.0 kB",
                1024 to "1.0 kB",
                1000000 to "1.0 MB").forEach { (key, value) ->
            DisplayUtils.byteSizeToString(key.toLong()) shouldBe value
        }
    }
})

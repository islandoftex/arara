// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.core.configuration.UserInterfaceOptions
import org.islandoftex.arara.core.session.Session
import kotlin.test.Test
import kotlin.test.assertEquals

class DisplayUtilsTest {
    @Test
    fun `formats byte sizes correctly`() {
        val savedOptions = Session.userInterfaceOptions
        try {
            Session.userInterfaceOptions = UserInterfaceOptions(MPPLocale("en"))
            mapOf(
                800 to "800 B",
                1000 to "1.0 kB",
                1024 to "1.0 kB",
                1000000 to "1.0 MB"
            ).forEach { (key, value) ->
                assertEquals(
                    value, DisplayUtils.byteSizeToString(key.toLong()),
                    "formatted size for $key bytes"
                )
            }
        } finally {
            // restore the session state no matter whether the formatting
            // assertions held
            Session.userInterfaceOptions = savedOptions
        }
    }
}

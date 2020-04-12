// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import java.util.Locale
import org.islandoftex.arara.api.configuration.UserInterfaceOptions

data class UserInterfaceOptions(
    override val locale: Locale = Locale("en"),
    override val swingLookAndFeel: String = "none"
) : UserInterfaceOptions

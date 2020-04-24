// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import com.uchuhimo.konf.ConfigSpec
import org.islandoftex.arara.api.configuration.LoggingOptions
import org.islandoftex.arara.api.configuration.UserInterfaceOptions

/**
 * Configuration hierarchy for arara
 *
 * @author Island of TeX
 * @version 5.0
 * @since 5.0
 */
@Suppress("MagicNumber")
object AraraSpec : ConfigSpec() {
    val loggingOptions by optional<LoggingOptions>(
            org.islandoftex.arara.core.configuration.LoggingOptions()
    )
    val userInterfaceOptions by optional<UserInterfaceOptions>(
            org.islandoftex.arara.core.configuration.UserInterfaceOptions()
    )
}

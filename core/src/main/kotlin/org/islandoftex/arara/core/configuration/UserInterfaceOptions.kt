// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Implements the user interface options model, covering the locale
 * and the terminal output width.
 */
data class UserInterfaceOptions(
    /**
     * The locale messages are localized for.
     */
    override val locale: MPPLocale = MPPLocale("en"),
    /**
     * The width terminal output wraps at, in columns.
     */
    override val terminalOutputWidth: Int = 65
) : UserInterfaceOptions

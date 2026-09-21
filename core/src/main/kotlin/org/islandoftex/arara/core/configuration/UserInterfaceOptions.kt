// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Implements the user interface options model, covering the locale, the
 * Swing look and feel and the terminal output width.
 */
data class UserInterfaceOptions(
    override val locale: MPPLocale = MPPLocale("en"),
    override val swingLookAndFeel: String = "none",
    override val terminalOutputWidth: Int = 65
) : UserInterfaceOptions

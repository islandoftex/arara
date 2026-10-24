// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import org.islandoftex.arara.api.localization.MPPLocale

/** Locale and terminal-presentation settings. */
data class UserInterfaceOptions(
    /** Locale used to select application messages. */
    override val locale: MPPLocale = MPPLocale("en"),
    /** Maximum requested output width, in terminal columns. */
    override val terminalOutputWidth: Int = 65,
    /** Configured console mode, or null to use the presentation default. */
    override val consoleMode: String? = null,
    /** Whether the styled renderer may animate live task feedback. */
    override val enableAnimations: Boolean = true
) : UserInterfaceOptions

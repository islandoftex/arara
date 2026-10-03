// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import org.islandoftex.arara.api.configuration.UserInterfaceOptions
import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Implements the user interface options model, covering the
 * locale, the terminal output width, and the live feedback
 * behavior.
 */
data class UserInterfaceOptions(
    /**
     * The locale messages are localized for.
     */
    override val locale: MPPLocale = MPPLocale("en"),
    /**
     * The width terminal output wraps at, in columns.
     */
    override val terminalOutputWidth: Int = 79,
    /**
     * Whether the terminal presentations may animate their live
     * feedback while a task runs. The breath is on by default;
     * the animations key silences it for those who prefer
     * stillness.
     */
    override val enableAnimations: Boolean = true
) : UserInterfaceOptions

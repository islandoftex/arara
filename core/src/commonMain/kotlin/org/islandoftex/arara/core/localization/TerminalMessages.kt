// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for terminal messages. */
interface TerminalMessages {
    /** Unknown console presentation; argument: supplied mode. */
    fun errorConsoleModeUnknown(mode: String): String

    /** Configured console width leaves insufficient space for task labels and results. */
    fun errorTerminalWidthInvalid(minimum: Int): String
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for terminal messages. */
public interface TerminalMessages {
    /** Unknown console presentation; argument: supplied mode. */
    public fun errorConsoleModeUnknown(mode: String): String

    /** Configured console width leaves insufficient space for task labels and results. */
    public fun errorTerminalWidthInvalid(minimum: Int): String
}

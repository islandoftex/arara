// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.configuration

import org.islandoftex.arara.api.localization.MPPLocale

/**
 * Configure arara's user interface
 */
public interface UserInterfaceOptions {
    /**
     * The locale of the language arara should use for output. If you specify a
     * language that is not available at runtime, English will be used as
     * fallback.
     */
    public val locale: MPPLocale

    /**
     * The name of the look and feel to apply to Swing components (dialogs).
     * If it is not available on the JVM at runtime, the default look and feel
     * will be used.
     */
    public val swingLookAndFeel: String

    /**
     * The number of columns to use as the default terminal width when
     * outputting to terminal. This will only be used for wrapping arara's
     * messages not the messages of external tools that are called.
     */
    public val terminalOutputWidth: Int
}

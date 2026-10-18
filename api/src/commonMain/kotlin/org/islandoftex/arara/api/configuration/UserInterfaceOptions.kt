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
     * The number of columns to use as the default terminal width when
     * outputting to terminal. This will only be used for wrapping arara's
     * messages not the messages of external tools that are called.
     */
    public val terminalOutputWidth: Int
}

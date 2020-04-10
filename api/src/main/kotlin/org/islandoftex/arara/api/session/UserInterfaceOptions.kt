// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.session

/**
 * Configure arara's user interface
 */
interface UserInterfaceOptions {
    /**
     * The ISO 639-1 code of the language arara should use for output. If
     * you specify a language that is not available at runtime, English will
     * be used as fallback.
     */
    val languageCode: String

    /**
     * The name of the look and feel to apply to Swing components (dialogs).
     * If it is not available on the JVM at runtime, the default look and feel
     * will be used.
     */
    val swingLookAndFeel: String
}

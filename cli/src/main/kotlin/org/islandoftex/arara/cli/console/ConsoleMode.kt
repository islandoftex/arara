// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Defines the console presentations the application offers and the
 * resolution that picks one: the command line wins over the
 * configuration file, which wins over the plain default - the
 * layout the tool has printed since its first release. A mode the
 * configuration names but the application does not offer is an
 * error rather than a silent fallback.
 *
 * @author Island of TeX
 */
enum class ConsoleMode {
    /**
     * The historic console layout, kept byte-identical.
     */
    PLAIN;

    /**
     * The renderer this presentation prints through.
     */
    val renderer: ConsoleRenderer
        get() = when (this) {
            PLAIN -> PlainConsoleRenderer
        }

    /**
     * The resolution that picks the console presentation from the
     * command line value and the configuration value.
     */
    companion object {
        /**
         * Resolves the console mode from the command line value and
         * the configured value.
         *
         * @param commandLine The mode the command line selected, if
         * any; the choice validation guarantees an offered name.
         * @param configured The mode the configuration file names, if
         * any; an unoffered name is an error.
         * @return The resolved console mode.
         */
        fun resolve(
            commandLine: ConsoleMode?,
            configured: String?
        ): ConsoleMode = commandLine ?: configured?.let { name ->
            when (name) {
                "plain" -> PLAIN
                else -> throw AraraException(
                    LanguageController.messages.errorConsoleModeUnknown(name)
                )
            }
        } ?: PLAIN
    }
}

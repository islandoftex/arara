// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import com.github.ajalt.mordant.terminal.Terminal
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer
import org.islandoftex.arara.cli.console.plain.PlainDialogRenderer
import org.islandoftex.arara.cli.console.styled.StyledConsoleRenderer
import org.islandoftex.arara.cli.console.styled.StyledDialogRenderer
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.ui.DialogRenderer

/**
 * Defines the console presentations the application offers and the
 * resolution that picks one: the command line wins over the
 * configuration file, which wins over the plain default - the
 * layout the tool has printed since its first release. A mode the
 * configuration names but the application does not offer is an
 * error rather than a silent fallback.
 */
enum class ConsoleMode {
    /**
     * The historic console layout, refreshed in place: the banner
     * names the run with the shared version line, the error blocks
     * title their sections on the dashed rule, and the run closes
     * with a verdict word - the silhouette itself stays the one
     * the previous major versions printed.
     */
    PLAIN,

    /**
     * The modern console presentation: the tinted banner, the
     * section rules, the glyphs and the colors of the detected
     * terminal, degrading to the plain structure when the stream
     * cannot carry them.
     */
    STYLED;

    /**
     * The renderer this presentation prints through.
     *
     * @param terminal The terminal the presentation renders on.
     * @return The console renderer of the presentation.
     */
    fun renderer(terminal: Terminal): ConsoleRenderer = when (this) {
        PLAIN -> PlainConsoleRenderer
        STYLED -> StyledConsoleRenderer(terminal)
    }

    /**
     * The dialog renderer this presentation converses through.
     *
     * @param terminal The terminal the presentation renders on.
     * @return The dialog renderer of the presentation.
     */
    fun dialogRenderer(terminal: Terminal): DialogRenderer = when (this) {
        PLAIN -> PlainDialogRenderer
        STYLED -> StyledDialogRenderer(terminal)
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
                "styled" -> STYLED
                else -> throw AraraException(
                    LanguageController.messages.errorConsoleModeUnknown(name)
                )
            }
        } ?: PLAIN
    }
}

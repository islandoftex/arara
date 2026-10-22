// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import com.github.ajalt.mordant.terminal.Terminal
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.cli.console.plain.PlainConsoleRenderer
import org.islandoftex.arara.cli.console.styled.StyledConsoleRenderer
import org.islandoftex.arara.core.localization.LanguageController

/** CLI selection overrides configuration; plain is the default. */
enum class ConsoleMode {
    PLAIN, STYLED;

    fun renderer(terminal: Terminal): ConsoleRenderer = when (this) {
        PLAIN -> PlainConsoleRenderer()
        STYLED -> StyledConsoleRenderer(terminal)
    }

    companion object {
        fun resolve(commandLine: ConsoleMode?, configured: String?): ConsoleMode = commandLine ?: when (configured) {
            null, "plain" -> PLAIN
            "styled" -> STYLED
            else -> throw AraraException(
                LanguageController.messages.errorConsoleModeUnknown(configured)
            )
        }
    }
}

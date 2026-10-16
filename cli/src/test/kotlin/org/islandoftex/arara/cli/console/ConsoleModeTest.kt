// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.console

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Verifies the console mode resolution: the command line wins over
 * the configuration file, which wins over the plain default, and a
 * mode the configuration names but the application does not offer
 * is an error rather than a silent fallback.
 */
class ConsoleModeTest {
    @Test
    fun `the command line wins over the configuration`() {
        assertEquals(
            ConsoleMode.PLAIN,
            ConsoleMode.resolve(ConsoleMode.PLAIN, "nonsense"),
            "the command line choice is final"
        )
    }

    @Test
    fun `the configuration names the plain mode`() {
        assertEquals(
            ConsoleMode.PLAIN,
            ConsoleMode.resolve(null, "plain"),
            "the configured plain mode resolves"
        )
    }

    @Test
    fun `the configuration names the styled mode`() {
        assertEquals(
            ConsoleMode.STYLED,
            ConsoleMode.resolve(null, "styled"),
            "the configured styled mode resolves"
        )
    }

    @Test
    fun `the command line styled choice is final`() {
        assertEquals(
            ConsoleMode.STYLED,
            ConsoleMode.resolve(ConsoleMode.STYLED, "nonsense"),
            "the command line choice is final"
        )
    }

    @Test
    fun `the plain default holds`() {
        assertEquals(
            ConsoleMode.PLAIN,
            ConsoleMode.resolve(null, null),
            "no selection keeps the plain default"
        )
    }

    @Test
    fun `an unoffered configured mode fails loudly`() {
        val exception = assertFailsWith<AraraException>(
            "a mode the application does not offer is an error"
        ) {
            ConsoleMode.resolve(null, "fancy")
        }
        assertEquals(
            LanguageController.messages.errorConsoleModeUnknown("fancy"),
            exception.message,
            "the localized unoffered-mode error carries"
        )
    }
}

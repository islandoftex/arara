// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.core.localization.LanguageController

/**
 * Verifies the default dialog renderer: a run with no dialog
 * support configured meets a clear, localized error on every
 * dialog kind, stating that the host application has to provide
 * the implementation - the library assumes no interactive surface.
 */
class HeadlessDialogRendererTest {
    private fun assertUnsupported(block: () -> Unit) {
        val exception = assertFailsWith<AraraException>(
            "a dialog without configured support is an error", block
        )
        assertEquals(
            LanguageController.messages.errorDialogsNotConfigured,
            exception.message,
            "the localized not-configured error carries"
        )
    }

    @Test
    fun `a message dialog raises the not-configured error`() {
        assertUnsupported {
            HeadlessDialogRenderer.showMessage(1, "Greetings", "This is a dialog")
        }
    }

    @Test
    fun `an options dialog raises the not-configured error`() {
        assertUnsupported {
            HeadlessDialogRenderer.showOptions(
                4, "Keep going?", "Shall we finish?", arrayOf("Yes", "No")
            )
        }
    }

    @Test
    fun `an input dialog raises the not-configured error`() {
        assertUnsupported {
            HeadlessDialogRenderer.showInput(4, "Your name", "What should I call you?")
        }
    }

    @Test
    fun `a dropdown dialog raises the not-configured error`() {
        assertUnsupported {
            HeadlessDialogRenderer.showDropdown(
                2, "Pick one", "Choose an engine",
                arrayOf("pdflatex", "lualatex")
            )
        }
    }
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ui

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.cli.console.styled.StyledDialogRenderer
import org.islandoftex.arara.core.localization.LanguageController
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocalizedDialogTest {
    @Test
    fun bothPresentationsUseTheCurrentCatalogAndKeepRuleTextLiteral() {
        val previous = LanguageController.messages
        try {
            for (locale in listOf("en", "de", "it", "nl", "pt-BR", "en-QN")) {
                LanguageController.loadMessagesFor(MPPLocale(locale))
                val messages = LanguageController.messages
                for (styled in listOf(false, true)) {
                    val output = StringWriter()
                    val input = StringReader("bad\n0\n0\n").buffered()
                    val writer = PrintWriter(output)
                    val renderer = if (styled) {
                        StyledDialogRenderer(Terminal(AnsiLevel.NONE, width = 160), input, writer)
                    } else {
                        TerminalDialogRenderer(input, writer, { 160 })
                    }
                    assertEquals(0, renderer.showOptions(3, "literal %s", "body %s", arrayOf("choice %s")))
                    assertEquals(0, renderer.showList(4, "literal %s", "body %s", arrayOf("choice %s")))
                    renderer.showMessage(2, "literal %s", "body %s")
                    renderer.showMessage(1, "literal %s", "body %s")
                    val error = assertFailsWith<AraraException> {
                        renderer.showInput(0, "literal %s", "body %s")
                    }
                    assertEquals(messages.errorDialogInputClosed, error.message)
                    val text = output.toString()
                    for (label in listOf(messages.infoLabelOnError, messages.infoLabelOnInfo,
                        messages.infoLabelOnWarning, messages.infoLabelOnQuestion)) {
                        assertTrue("[$label] literal %s" in text, "$locale: $text")
                    }
                    assertTrue("${messages.infoLabelDialogPrompt} (0-1):" in text)
                    assertTrue("choice %s" in text && "body %s" in text)
                }
            }
        } finally {
            LanguageController.messages = previous
        }
    }
}

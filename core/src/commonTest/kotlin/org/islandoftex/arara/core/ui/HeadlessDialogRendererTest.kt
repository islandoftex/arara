// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.localization.MPPLocale
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import org.islandoftex.arara.core.localization.LanguageController

class HeadlessDialogRendererTest {
    @Test
    fun everyDialogRequiresAnExplicitHost() {
        val calls = listOf<() -> Any>(
            { HeadlessDialogRenderer.showMessage(type = 0, title = "Title", text = "Body") },
            { HeadlessDialogRenderer.showInput(type = 0, title = "Title", text = "Body") },
            { HeadlessDialogRenderer.showOptions(type = 0, title = "Title", text = "Body", buttons = emptyArray()) },
            { HeadlessDialogRenderer.showList(type = 0, title = "Title", text = "Body", elements = emptyArray()) }
        )
        val previous = LanguageController.messages
        try {
            for (locale in listOf("en", "de", "it", "nl", "pt-BR", "en-QN")) {
                LanguageController.loadMessagesFor(MPPLocale(locale))
                calls.forEach { call ->
                    val error = assertFailsWith<AraraException> { call() }
                    assertEquals(LanguageController.messages.ERROR_DIALOGS_NOT_CONFIGURED, error.message)
                }
            }
        } finally {
            LanguageController.messages = previous
        }
    }
}

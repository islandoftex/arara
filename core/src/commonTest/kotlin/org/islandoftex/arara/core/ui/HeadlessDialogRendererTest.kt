// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.ui

import org.islandoftex.arara.api.AraraException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HeadlessDialogRendererTest {
    @Test
    fun everyDialogRequiresAnExplicitHost() {
        val calls = listOf<() -> Any>(
            { HeadlessDialogRenderer.showMessage(type = 0, title = "Title", text = "Body") },
            { HeadlessDialogRenderer.showInput(type = 0, title = "Title", text = "Body") },
            { HeadlessDialogRenderer.showOptions(type = 0, title = "Title", text = "Body", buttons = emptyArray()) },
            { HeadlessDialogRenderer.showList(type = 0, title = "Title", text = "Body", elements = emptyArray()) }
        )
        calls.forEach { call ->
            val error = assertFailsWith<AraraException> { call() }
            assertTrue(error.message.orEmpty().contains("host"))
        }
    }
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.utils

import org.islandoftex.arara.core.ui.DialogRenderer
import org.islandoftex.arara.core.ui.Dialogs
import kotlin.test.Test
import kotlin.test.assertEquals

class DialogDelegationTest {
    @Test
    fun helpersUseTheCurrentHostAndPreserveArgumentsAndResults() {
        val previous = Dialogs.renderer
        // Initialize helpers before installing the host to catch captured implementations.
        val rules = RuleMethods
        val conditions = ConditionalMethods
        val calls = mutableListOf<List<Any>>()
        Dialogs.renderer = object : DialogRenderer {
            override val defaultWidth: Int = 123
            override fun showMessage(width: Int, type: Int, title: String, text: String) {
                calls.add(listOf(width, type, title, text))
            }
            override fun showOptions(width: Int, type: Int, title: String, text: String, buttons: Array<out Any>): Int {
                calls.add(listOf(width, type, title, text, buttons.toList()))
                return 2
            }
            override fun showInput(width: Int, type: Int, title: String, text: String): String = "answer"
            override fun showDropdown(width: Int, type: Int, title: String, text: String, elements: Array<out Any>): Int = 1
        }
        try {
            rules.showMessage(type = 2, title = "Title", text = "Body")
            assertEquals(listOf(123, 2, "Title", "Body"), calls.single())
            assertEquals(2, conditions.showOptions(99, 3, "Choose", "Text", *arrayOf("A", "B")))
            assertEquals(listOf(99, 3, "Choose", "Text", listOf("A", "B")), calls.last())
            assertEquals("answer", conditions.showInput(type = 4, title = "Input", text = "Text"))
            assertEquals(1, conditions.showDropdown(type = 4, title = "List", text = "Text", elements = arrayOf("A")))
        } finally {
            Dialogs.renderer = previous
        }
    }
}

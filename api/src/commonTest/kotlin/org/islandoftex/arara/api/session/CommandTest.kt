// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.session

import kotlin.test.Test
import kotlin.test.assertNull

class CommandTest {

    @Test
    fun commandsWithoutAnExplicitLabelRemainUnnamed() {
        val command = object : Command {
            override val elements = listOf("latex", "document.tex")
            override val workingDirectory = null
        }

        assertNull(command.displayName)
    }
}

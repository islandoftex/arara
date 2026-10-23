// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import kotlin.test.Test
import kotlin.test.assertEquals

class DirectiveAssemblerTest {
    @Test
    fun capturedLineNumbersDoNotChangeWhenAssemblyContinues() {
        val assembler = DirectiveAssembler()
        assembler.addLineNumber(1)
        val first = assembler.getLineNumbers()
        assembler.addLineNumber(3)
        assertEquals(listOf(1), first)
        assertEquals(listOf(1, 3), assembler.getLineNumbers())
    }
}

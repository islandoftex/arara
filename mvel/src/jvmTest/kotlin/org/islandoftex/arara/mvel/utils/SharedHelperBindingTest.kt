// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.utils

import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.core.rules.RuleMethods
import org.mvel2.templates.TemplateRuntime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SharedHelperBindingTest {
    @Test
    fun movedHelpersKeepTheirNamesAndOverloadsInMvelExpressions() {
        assertEquals("one two", TemplateRuntime.eval(
            "@{ buildString(trimSpaces(' one '), 'two') }", MvelState.ruleMethods
        ))
        val command = TemplateRuntime.eval(
            "@{ getCommandWithWorkingDirectory('.', 'pdflatex', '--synctex=1') }", MvelState.ruleMethods
        ) as Command
        assertEquals(listOf("pdflatex", "--synctex=1"), command.elements)
        assertEquals(true, TemplateRuntime.eval("@{ isTrue('yes') }", MvelState.ruleMethods))
    }

    @Test
    fun authoredErrorsKeepTheSharedContextualException() {
        val error = assertFailsWith<AraraExceptionWithHeader> { RuleMethods.throwError("rule sentinel") }
        assertEquals("rule sentinel", error.message)
    }
}

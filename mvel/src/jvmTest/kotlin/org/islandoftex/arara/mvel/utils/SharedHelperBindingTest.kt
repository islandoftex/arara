// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.utils

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.core.rules.RuleMethods
import org.mvel2.templates.TemplateRuntime

class SharedHelperBindingTest {
    @Test
    fun suppliedPathSpellingIsSeparateFromMvelPathDisplay() {
        val spelling = "./nested//../chapter.tex"
        val path = MPPPath(spelling)
        val context = MvelState.ruleMethods + ("reference" to path)
        assertEquals(spelling, TemplateRuntime.eval("@{ reference.suppliedSpelling }", context))
        assertEquals(path.toString(), TemplateRuntime.eval("@{ reference.toString() }", context))
    }

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

    @Test
    fun consolidatedHelpersKeepTheirReflectiveOverloads() {
        val root = Files.createTempDirectory("arara MVEL helpers ").toMPPPath()
        val target = root.resolve("output.txt")
        try {
            for (value in listOf(target, target.toString())) {
                val context = MvelState.ruleMethods + ("target" to value)
                assertEquals(true, TemplateRuntime.eval(
                    "@{ writeToFile(target, replicatePattern('%s!', ['one', 'two']), false) }", context
                ))
                assertEquals(listOf("one!", "two!"), TemplateRuntime.eval("@{ readFromFile(target) }", context))
                assertEquals(true, TemplateRuntime.eval("@{ writeToFile(target, 'three', false) }", context))
                assertEquals("three", target.readText())
            }
            val context = MvelState.conditionalMethods + ("target" to target)
            assertEquals(true, TemplateRuntime.eval("@{ found(target, 'three') }", context))
            assertEquals(false, TemplateRuntime.eval("@{ found(target, 'absent') }", context))
            val failed = MvelState.ruleMethods + ("target" to root)
            assertEquals(false, TemplateRuntime.eval("@{ writeToFile(target, 'nope', false) }", failed))
        } finally {
            root.removeDirectory()
        }
    }
}

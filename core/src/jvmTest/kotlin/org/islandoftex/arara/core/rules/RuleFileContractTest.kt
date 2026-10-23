// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import java.nio.file.Files
import java.util.MissingFormatArgumentException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.localization.GermanMessages
import org.islandoftex.arara.core.localization.LanguageController

class RuleFileContractTest {
    private fun inDirectory(work: (MPPPath) -> Unit) {
        val root = Files.createTempDirectory("arara helper files ").toMPPPath()
        try {
            work(root)
        } finally {
            root.removeDirectory()
        }
    }

    @Test
    fun fileNamesAndTypesAcceptBothPathOverloadsAndRejectDirectories() = inDirectory { root ->
        val input = root.resolve("paper.draft.tex").apply { writeText("") }
        assertEquals("paper.draft", RuleMethods.getBasename(input))
        assertEquals("paper.draft", RuleMethods.getBasename(input.toString()))
        assertEquals("tex", RuleMethods.getFiletype(input))
        assertEquals("tex", RuleMethods.getFiletype(input.toString()))
        assertFailsWith<AraraException> { RuleMethods.getBasename(root) }
        assertFailsWith<AraraException> { RuleMethods.getBasename(root.toString()) }
        assertFailsWith<AraraException> { RuleMethods.getFiletype(root) }
        assertFailsWith<AraraException> { RuleMethods.getFiletype(root.toString()) }
    }

    @Test
    fun explicitCommandBuildersFlattenListsAndKeepTheWorkingDirectory() = inDirectory { root ->
        val commands = listOf(
            RuleMethods.getCommandWithWorkingDirectory(root, "pdflatex", listOf("-opt"), null, ""),
            RuleMethods.getCommandWithWorkingDirectory(root.toString(), "pdflatex", listOf("-opt"), null, ""),
            RuleMethods.getCommandWithWorkingDirectory(root, listOf("pdflatex", "-opt")),
            RuleMethods.getCommandWithWorkingDirectory(root.toString(), listOf("pdflatex", "-opt"))
        )
        commands.forEach { command ->
            assertEquals(listOf("pdflatex", "-opt"), command.elements)
            assertEquals(root, command.workingDirectory)
        }
    }

    @Test
    fun listingsRespectTheirFiltersAndRecursionForBothPathOverloads() = inDirectory { root ->
        root.resolve("a.tex").writeText("a")
        root.resolve("b.log").writeText("b")
        val nested = root.resolve("nested").apply { createDirectories() }
        nested.resolve("c.tex").writeText("c")
        assertEquals(setOf("a.tex"), RuleMethods.listFilesByExtensions(root, listOf("tex"), false)
            .map { it.fileName }.toSet())
        assertEquals(setOf("a.tex", "c.tex"),
            RuleMethods.listFilesByExtensions(root.toString(), listOf("tex"), true).map { it.fileName }.toSet())
        assertEquals(setOf("b.log"), RuleMethods.listFilesByPatterns(root, listOf("*.log"), false)
            .map { it.fileName }.toSet())
        assertEquals(setOf("a.tex", "c.tex"),
            RuleMethods.listFilesByPatterns(root.toString(), listOf("*.tex"), true).map { it.fileName }.toSet())
    }

    @Test
    fun writesPreserveUtf8AppendOverwriteAndNativeLineSeparators() = inDirectory { root ->
        val text = root.resolve("text.txt")
        assertTrue(RuleMethods.writeToFile(text, "über", false))
        assertTrue(RuleMethods.writeToFile(text.toString(), " café", true))
        assertEquals("über café", text.readText())
        assertTrue(RuleMethods.writeToFile(text, "replacement", false))
        assertEquals("replacement", text.readText())
        val lines = root.resolve("lines.txt")
        assertTrue(RuleMethods.writeToFile(lines, listOf("one", "two"), false))
        assertEquals("one${System.lineSeparator()}two", lines.readText())
        assertTrue(RuleMethods.writeToFile(lines.toString(), listOf("three", "four"), false))
        assertEquals("three${System.lineSeparator()}four", lines.readText())
    }

    @Test
    fun writeFailuresReturnFalseForDirectoriesAndBlockedParents() = inDirectory { root ->
        assertFalse(RuleMethods.writeToFile(root, "nope", false))
        assertFalse(RuleMethods.writeToFile(root.toString(), listOf("nope"), false))
        val blocker = root.resolve("blocker").apply { writeText("unchanged") }
        assertFalse(RuleMethods.writeToFile(blocker.resolve("child"), "nope", false))
        assertFalse(RuleMethods.writeToFile(blocker.resolve("child").toString(), "nope", false))
        assertEquals("unchanged", blocker.readText())
    }

    @Test
    fun readsSplitNativeLineEndingsAndReturnAnEmptyListOnPathFailures() = inDirectory { root ->
        val input = root.resolve("input.txt").apply { writeText("first\r\nsecond\nthird") }
        assertEquals(listOf("first", "second", "third"), RuleMethods.readFromFile(input))
        assertEquals(RuleMethods.readFromFile(input), RuleMethods.readFromFile(input.toString()))
        assertEquals(emptyList(), RuleMethods.readFromFile(root.resolve("missing.txt")))
        assertEquals(emptyList(), RuleMethods.readFromFile(root.toString()))
    }

    @Test
    fun regexSearchDistinguishesMatchesMissingFilesAndInvalidPatterns() = inDirectory { root ->
        val input = root.resolve("input.log").apply { writeText("error in line 3\n") }
        assertTrue(ConditionalMethods.found(input, "error.*3"))
        assertFalse(ConditionalMethods.found(input, "warning"))
        for (pattern in listOf("error", "[")) {
            val failure = assertFailsWith<AraraException> {
                ConditionalMethods.found(root.resolve("missing.log"), pattern)
            }
            assertTrue(failure.message.orEmpty().contains("missing.log"))
            assertIs<AraraIOException>(failure.cause)
        }
        assertFailsWith<IllegalArgumentException> { ConditionalMethods.found(input, "[") }
        assertEquals("input.log", ConditionalMethods.toFile(input.toString()).fileName)
        assertTrue(ConditionalMethods.exists(input))
        assertFalse(ConditionalMethods.missing(input))
        assertFalse(ConditionalMethods.exists(root.resolve("missing.log")))
        assertTrue(ConditionalMethods.missing(root.resolve("missing.log")))
    }

    @Test
    fun invalidReplicationRetainsItsFormattingCause() {
        val error = assertFailsWith<AraraException> { RuleMethods.replicatePattern("%s %s", listOf("one")) }
        assertIs<MissingFormatArgumentException>(error.exception)
    }

    @Test
    fun invalidGlobsAreLocalizedAndRetainTheParserCause() = inDirectory { root ->
        val previous = LanguageController.messages
        try {
            val messages = GermanMessages
            LanguageController.messages = messages
            val failure = assertFailsWith<AraraException> {
                RuleMethods.listFilesByPatterns(root, listOf("["), false)
            }
            val cause = assertIs<IllegalArgumentException>(failure.cause)
            assertEquals(messages.errorGlobPatternInvalid("[", cause.message.orEmpty()), failure.message)
        } finally {
            LanguageController.messages = previous
        }
    }
}

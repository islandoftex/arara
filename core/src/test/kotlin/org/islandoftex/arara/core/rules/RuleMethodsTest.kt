// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.SafeRunViolationException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session

/**
 * Verifies the rule helper surface shared by both engine dialects:
 * string and boolean helpers, command building, file listing, reading
 * and writing, type checks and the system family. The helpers that
 * read the executor state are exercised inside the lifecycle window
 * in which real directives run; the interactive dialog surface is
 * deliberately out of scope here, as the dialog suites verify it
 * whole.
 */
class RuleMethodsTest {
    @Test
    fun `trims spaces from strings`() {
        assertEquals("arara", RuleMethods.trimSpaces("  arara  "))
    }

    @Test
    fun `retrieves list elements safely`() {
        val list = listOf("first", "second")
        assertEquals("first", RuleMethods.getOrNull(list))
        assertEquals("second", RuleMethods.getOrNull(list, 1))
        assertNull(RuleMethods.getOrNull(list, 5))
        assertNull(RuleMethods.getOrNull(emptyList()))
    }

    @Test
    fun `builds strings from mixed objects`() {
        assertEquals("arara 6", RuleMethods.buildString("arara", 6))
        assertEquals("arara", RuleMethods.buildString("arara", null, ""))
        assertEquals("", RuleMethods.buildString(null, ""))
    }

    @Test
    fun `gets the basename of existing files`() {
        val file = Files.createTempFile("arara-basename-", ".tex")
        val name = file.fileName.toString().substringBeforeLast('.')
        assertEquals(name, RuleMethods.getBasename(MPPPath(file.toString())))
        assertEquals(name, RuleMethods.getBasename(file.toString()))
    }

    @Test
    fun `rejects the basename of directories`() {
        val directory = Files.createTempDirectory("arara-basename-dir")
        assertFailsWith<AraraException> { RuleMethods.getBasename(MPPPath(directory.toString())) }
        assertFailsWith<AraraException> { RuleMethods.getBasename(directory.toString()) }
    }

    @Test
    fun `gets the file type of existing files`() {
        val file = Files.createTempFile("arara-filetype-", ".log")
        assertEquals("log", RuleMethods.getFiletype(MPPPath(file.toString())))
        assertEquals("log", RuleMethods.getFiletype(file.toString()))
    }

    @Test
    fun `rejects the file type of directories`() {
        val directory = Files.createTempDirectory("arara-filetype-dir")
        assertFailsWith<AraraException> { RuleMethods.getFiletype(MPPPath(directory.toString())) }
        assertFailsWith<AraraException> { RuleMethods.getFiletype(directory.toString()) }
    }

    @Test
    fun `replicates patterns over values`() {
        assertEquals(listOf<Any>("a", "b"), RuleMethods.replicatePattern("%s", listOf("a", "b")))
        assertEquals(listOf<Any>("1", "2"), RuleMethods.replicatePattern("%s", listOf(1, 2)))
    }

    @Test
    fun `rejects patterns with missing format specifiers`() {
        assertFailsWith<AraraException> {
            RuleMethods.replicatePattern("%s %s", listOf("lonely"))
        }
    }

    @Test
    fun `throws the header exception on demand`() {
        val exception = assertFailsWith<AraraException> { RuleMethods.throwError("boom") }
        assertEquals("boom", exception.message)
    }

    @Test
    fun `exposes the session`() {
        assertSame(Session, RuleMethods.getSession())
    }

    @Test
    fun `builds commands with explicit working directories`() {
        val directory = Files.createTempDirectory("arara-command-dir")
        val flattened = RuleMethods.getCommandWithWorkingDirectory(
            directory.toString(), "pdflatex", listOf("-opt"), null, ""
        )
        assertEquals(listOf("pdflatex", "-opt"), flattened.elements)
        assertEquals(MPPPath(directory.toString()), flattened.workingDirectory)
        val byPath = RuleMethods.getCommandWithWorkingDirectory(
            MPPPath(directory.toString()), "pdflatex", listOf("-opt")
        )
        assertEquals(listOf("pdflatex", "-opt"), byPath.elements)
        assertEquals(MPPPath(directory.toString()), byPath.workingDirectory)
        val listed = RuleMethods.getCommandWithWorkingDirectory(
            directory.toString(), listOf("pdflatex")
        )
        assertEquals(listOf("pdflatex"), listed.elements)
        val listedByPath = RuleMethods.getCommandWithWorkingDirectory(
            MPPPath(directory.toString()), listOf("pdflatex")
        )
        assertEquals(listOf("pdflatex"), listedByPath.elements)
    }

    @Test
    fun `the executor state feeds the command and reference helpers`() {
        executeWithinProject(listOf("main.tex" to 0)) { directory, _ ->
            assertEquals("main.tex", RuleMethods.originalFile)
            assertEquals("main.tex", RuleMethods.originalReference.fileName)
            val command = RuleMethods.getCommand(listOf("pdflatex", "main.tex"))
            assertEquals(listOf("pdflatex", "main.tex"), command.elements)
            assertEquals(MPPPath(directory.toString()), command.workingDirectory)
            val flattened = RuleMethods.getCommand("pdflatex", listOf("-opt"), null, "")
            assertEquals(listOf("pdflatex", "-opt"), flattened.elements)
            assertEquals(MPPPath(directory.toString()), flattened.workingDirectory)
        }
    }

    @Test
    fun `the original reference stays the main file while the halt answers the running file`() {
        executeWithinProject(listOf("main.tex" to 0, "dep.tex" to 1)) { _, file ->
            if (file.path.fileName == "dep.tex") {
                assertEquals("main.tex", RuleMethods.originalFile)
                RuleMethods.halt(7)
                assertEquals(7, Session.get(LinearExecutor.haltKeyFor(file)))
            } else {
                RuleMethods.halt()
                assertEquals(0, Session.get(LinearExecutor.haltKeyFor(file)))
            }
        }
    }

    @Test
    fun `checks subdirectories against the project root`() {
        val outside = Files.createTempDirectory("arara-outside")
        executeWithinProject(listOf("main.tex" to 0)) { directory, _ ->
            val nested = Files.createDirectories(directory.resolve("nested"))
            assertTrue(RuleMethods.isSubdirectory(MPPPath(nested.toString())))
            assertFalse(RuleMethods.isSubdirectory(MPPPath(outside.toString())))
        }
    }

    @Test
    fun `reflects the verbose flag`() {
        val original = LinearExecutor.executionOptions
        try {
            LinearExecutor.executionOptions =
                ExecutionOptions.from(original).copy(verbose = true)
            assertTrue(RuleMethods.isVerboseMode())
            LinearExecutor.executionOptions =
                ExecutionOptions.from(original).copy(verbose = false)
            assertFalse(RuleMethods.isVerboseMode())
        } finally {
            LinearExecutor.executionOptions = original
        }
    }

    @Test
    fun `rejects system commands in safe mode`() {
        val original = LinearExecutor.executionOptions
        try {
            LinearExecutor.executionOptions =
                ExecutionOptions.from(original).copy(executionMode = ExecutionMode.SAFE_RUN)
            val directory = Files.createTempDirectory("arara-saferun")
            val command = RuleMethods.getCommandWithWorkingDirectory(
                directory.toString(), "pdflatex"
            )
            assertFailsWith<SafeRunViolationException> {
                RuleMethods.unsafelyExecuteSystemCommand(command)
            }
        } finally {
            LinearExecutor.executionOptions = original
        }
    }

    @Test
    fun `executes system commands and reports their outcome`() {
        val directory = Files.createTempDirectory("arara-system")
        val java = ProcessHandle.current().info().command().orElse("java")
        val completed = RuleMethods.unsafelyExecuteSystemCommand(
            RuleMethods.getCommandWithWorkingDirectory(directory.toString(), java, "-version")
        )
        assertEquals(0, completed.first)
        val failed = RuleMethods.unsafelyExecuteSystemCommand(
            RuleMethods.getCommandWithWorkingDirectory(
                directory.toString(), "arara-not-a-real-command-quack", "--flag"
            )
        )
        assertEquals(Environment.ERROR_EXIT_STATUS, failed.first)
        assertTrue("arara-not-a-real-command-quack" in failed.second)
    }

    @Test
    fun `lists files by extension`() {
        val tree = Files.createTempDirectory("arara-listing")
        Files.writeString(tree.resolve("a.tex"), "a")
        Files.writeString(tree.resolve("b.log"), "b")
        Files.createDirectories(tree.resolve("nested"))
        Files.writeString(tree.resolve("nested/c.tex"), "c")
        val flat = RuleMethods.listFilesByExtensions(
            MPPPath(tree.toString()), listOf("tex"), false
        )
        assertEquals(setOf("a.tex"), flat.map { it.fileName }.toSet())
        val deep = RuleMethods.listFilesByExtensions(
            tree.toString(), listOf("tex"), true
        )
        assertEquals(setOf("a.tex", "c.tex"), deep.map { it.fileName }.toSet())
    }

    @Test
    fun `lists files by pattern`() {
        val tree = Files.createTempDirectory("arara-patterns")
        Files.writeString(tree.resolve("a.tex"), "a")
        Files.writeString(tree.resolve("b.log"), "b")
        Files.createDirectories(tree.resolve("nested"))
        Files.writeString(tree.resolve("nested/c.tex"), "c")
        val flat = RuleMethods.listFilesByPatterns(
            MPPPath(tree.toString()), listOf("*.log"), false
        )
        assertEquals(setOf("b.log"), flat.map { it.fileName }.toSet())
        val deep = RuleMethods.listFilesByPatterns(
            tree.toString(), listOf("*.tex"), true
        )
        assertEquals(setOf("a.tex", "c.tex"), deep.map { it.fileName }.toSet())
    }

    @Test
    fun `writes text and lines to files`() {
        val directory = Files.createTempDirectory("arara-writing")
        val first = directory.resolve("first.txt")
        assertTrue(RuleMethods.writeToFile(MPPPath(first.toString()), "one", false))
        assertEquals("one", MPPPath(first.toString()).readText())
        val second = directory.resolve("second.txt")
        assertTrue(RuleMethods.writeToFile(second.toString(), "one", false))
        // appending is reserved for existing files, so the second call
        // accumulates instead of creating
        assertTrue(RuleMethods.writeToFile(second.toString(), "two", true))
        assertEquals("onetwo", MPPPath(second.toString()).readText())
        val third = directory.resolve("third.txt")
        assertTrue(RuleMethods.writeToFile(MPPPath(third.toString()), listOf("x", "y"), false))
        assertEquals("x${System.lineSeparator()}y", MPPPath(third.toString()).readText())
        val fourth = directory.resolve("fourth.txt")
        assertTrue(RuleMethods.writeToFile(fourth.toString(), listOf("x"), false))
        assertEquals("x", MPPPath(fourth.toString()).readText())
    }

    @Test
    fun `reports write failures`() {
        val directory = Files.createTempDirectory("arara-write-failure")
        // a directory is a structurally impossible target, but the
        // helper reports it like any other failure: a plain false,
        // the same probe semantics the reading helper keeps
        assertFalse(
            RuleMethods.writeToFile(MPPPath(directory.toString()), "nope", false)
        )
        assertFalse(RuleMethods.writeToFile(directory.toString(), "nope", false))
        // appending is reserved for existing files: a missing target
        // fails the same way instead of raising past the caller
        assertFalse(
            RuleMethods.writeToFile(directory.resolve("absent.txt").toString(), "x", true)
        )
        // a path nested under a regular file fails at the system level,
        // which the helper reports as a logical false
        val blocker = directory.resolve("blocker.txt")
        Files.writeString(blocker, "in the way")
        assertFalse(
            RuleMethods.writeToFile(MPPPath(blocker.resolve("nested.txt").toString()), "x", false)
        )
        assertFalse(RuleMethods.writeToFile(blocker.resolve("nested.txt").toString(), "x", false))
    }

    @Test
    fun `reads files into lines`() {
        val file = Files.createTempFile("arara-reading-", ".txt")
        Files.writeString(file, "first\nsecond")
        assertEquals(listOf("first", "second"), RuleMethods.readFromFile(MPPPath(file.toString())))
        assertEquals(listOf("first", "second"), RuleMethods.readFromFile(file.toString()))
        // missing and unreadable files fall back to the empty list, the
        // helper's intended probe semantics
        assertEquals(emptyList(), RuleMethods.readFromFile(file.toString() + ".missing"))
        assertEquals(
            emptyList(),
            RuleMethods.readFromFile(MPPPath(file.toString() + ".missing"))
        )
        val directory = Files.createTempDirectory("arara-reading-dir")
        assertEquals(emptyList(), RuleMethods.readFromFile(directory.toString()))
    }

    @Test
    fun `checks string emptiness`() {
        assertTrue(RuleMethods.isEmpty(""))
        assertFalse(RuleMethods.isEmpty("quack"))
        assertFalse(RuleMethods.isNotEmpty(""))
        assertTrue(RuleMethods.isNotEmpty("quack"))
        assertEquals("yes", RuleMethods.isEmpty("", "yes", "no"))
        assertEquals("no", RuleMethods.isEmpty("quack", "yes", "no"))
        assertEquals("", RuleMethods.isEmpty("quack", "yes"))
        assertEquals("yes", RuleMethods.isNotEmpty("quack", "yes", "no"))
        assertEquals("no", RuleMethods.isNotEmpty("", "yes", "no"))
    }

    @Test
    fun `interprets boolean strings`() {
        assertTrue(RuleMethods.isTrue("yes"))
        assertTrue(RuleMethods.isTrue("on"))
        assertFalse(RuleMethods.isTrue("no"))
        assertTrue(RuleMethods.isFalse("no"))
        assertFalse(RuleMethods.isTrue(""))
        assertFalse(RuleMethods.isFalse(""))
        assertFailsWith<AraraException> { RuleMethods.isTrue("quack") }
        assertFailsWith<AraraException> { RuleMethods.isFalse("quack") }
    }

    @Test
    fun `maps boolean strings to values`() {
        assertEquals("yes", RuleMethods.isTrue("yes", "yes", "no"))
        assertEquals("no", RuleMethods.isTrue("no", "yes", "no"))
        assertEquals("", RuleMethods.isTrue("no", "yes"))
        assertEquals("yes", RuleMethods.isFalse("no", "yes", "no"))
        assertEquals("no", RuleMethods.isFalse("yes", "yes", "no"))
        assertEquals("", RuleMethods.isFalse("yes", "yes"))
        assertEquals("fallback", RuleMethods.isTrue("", "yes", "no", "fallback"))
        assertEquals("yes", RuleMethods.isTrue("yes", "yes", "no", "fallback"))
        assertEquals("fallback", RuleMethods.isFalse("", "yes", "no", "fallback"))
        assertEquals("yes", RuleMethods.isFalse("no", "yes", "no", "fallback"))
    }

    @Test
    fun `maps boolean expressions to values`() {
        assertEquals("yes", RuleMethods.isTrue(true, "yes", "no"))
        assertEquals("no", RuleMethods.isTrue(false, "yes", "no"))
        assertEquals("", RuleMethods.isTrue(false, "yes"))
        assertEquals("yes", RuleMethods.isFalse(false, "yes", "no"))
        assertEquals("no", RuleMethods.isFalse(true, "yes", "no"))
        assertEquals("", RuleMethods.isFalse(true, "yes"))
    }

    @Test
    fun `checks types by class membership`() {
        assertTrue(RuleMethods.checkClass(String::class.java, "quack"))
        assertFalse(RuleMethods.checkClass(String::class.java, 42))
        assertTrue(RuleMethods.isString("quack"))
        assertFalse(RuleMethods.isString(42))
        assertTrue(RuleMethods.isList(listOf(1)))
        assertFalse(RuleMethods.isList("quack"))
        assertTrue(RuleMethods.isMap(mapOf(1 to 2)))
        assertFalse(RuleMethods.isMap(listOf(1)))
        // booleans live in the boxed class, so a primitive check would
        // always report false
        assertTrue(RuleMethods.isBoolean(true))
        assertFalse(RuleMethods.isBoolean(42))
        assertFalse(RuleMethods.isBoolean("true"))
    }

    @Test
    fun `looks commands up on the system path`() {
        assertFalse(RuleMethods.isOnPath("arara-not-a-real-command-quack"))
    }

    @Test
    fun `the system checks mirror each other`() {
        val windows = RuleMethods.isWindows()
        val linux = RuleMethods.isLinux()
        val mac = RuleMethods.isMac()
        assertEquals(
            1,
            listOf(windows, linux, mac).count { it },
            "exactly one desktop system matches the host"
        )
        assertEquals(linux || mac, RuleMethods.isUnix())
        assertEquals(if (windows) "yes" else "no", RuleMethods.isWindows("yes", "no"))
        assertEquals(
            if (RuleMethods.isCygwin()) "yes" else "no",
            RuleMethods.isCygwin("yes", "no")
        )
        assertEquals(if (linux) "yes" else "no", RuleMethods.isLinux("yes", "no"))
        assertEquals(if (mac) "yes" else "no", RuleMethods.isMac("yes", "no"))
        assertEquals(if (RuleMethods.isUnix()) "yes" else "no", RuleMethods.isUnix("yes", "no"))
    }
}

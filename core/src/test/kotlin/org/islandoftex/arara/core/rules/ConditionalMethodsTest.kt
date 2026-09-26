// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

import java.nio.file.Files
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.SafeRunViolationException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.session.ClassLoading.ClassLoadingStatus
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Verifies the conditional helper surface shared by both dialects:
 * existence and change checks against the file database, regex
 * searches, the current file reference and class loading. The
 * extension-based helpers are exercised inside the lifecycle window in
 * which real conditionals evaluate; the interactive dialog surface is
 * deliberately out of scope here, as its fate is under review.
 */
class ConditionalMethodsTest {
    @Test
    fun `returns file objects from names`() {
        assertEquals("quack.tex", ConditionalMethods.toFile("quack.tex").fileName)
    }

    @Test
    fun `checks file existence`() {
        val file = Files.createTempFile("arara-exists-", ".log")
        assertTrue(ConditionalMethods.exists(MPPPath(file.toString())))
        assertFalse(ConditionalMethods.missing(MPPPath(file.toString())))
        assertFalse(ConditionalMethods.exists(MPPPath("arara-not-there.log")))
        assertTrue(ConditionalMethods.missing(MPPPath("arara-not-there.log")))
    }

    @Test
    fun `the extension helpers resolve against the file under run`() {
        executeWithinProject(listOf("main.tex" to 0)) { directory, file ->
            Files.writeString(directory.resolve("main.log"), "error in line 3")
            assertTrue(ConditionalMethods.exists("log"))
            assertFalse(ConditionalMethods.exists("aux"))
            assertTrue(ConditionalMethods.missing("aux"))
            assertFalse(ConditionalMethods.missing("log"))
            assertTrue(ConditionalMethods.found("log", "error in"))
            assertFalse(ConditionalMethods.found("log", "warning"))
            assertEquals(file.path, ConditionalMethods.currentFile())
        }
    }

    @Test
    fun `the change checks consult the file database`() {
        executeWithinProject(listOf("main.tex" to 0)) { directory, _ ->
            Files.writeString(directory.resolve("main.aux"), "auxiliary content")
            assertTrue(ConditionalMethods.changed("aux"), "an unseen file counts as changed")
            assertFalse(ConditionalMethods.changed("aux"), "the recorded hash settles the file")
            assertTrue(ConditionalMethods.unchanged("aux"))
            val watched = directory.resolve("watch.log")
            Files.writeString(watched, "watched content")
            val target = MPPPath(watched.toString())
            assertTrue(ConditionalMethods.changed(target))
            assertFalse(ConditionalMethods.changed(target))
            assertTrue(ConditionalMethods.unchanged(target))
        }
    }

    @Test
    fun `the regex check reports matches and rejects unreadable files`() {
        val file = Files.createTempFile("arara-found-", ".log")
        Files.writeString(file, "error in line 3")
        assertTrue(ConditionalMethods.found(MPPPath(file.toString()), "error"))
        assertFalse(ConditionalMethods.found(MPPPath(file.toString()), "warning"))
        assertFailsWith<AraraException> {
            ConditionalMethods.found(MPPPath(file.toString() + ".missing"), "error")
        }
    }

    @Test
    fun `class loading is rejected in safe mode`() {
        val original = LinearExecutor.executionOptions
        try {
            LinearExecutor.executionOptions =
                ExecutionOptions.from(original).copy(executionMode = ExecutionMode.SAFE_RUN)
            val jar = classArchive("kotlin/Unit.class")
            assertFailsWith<SafeRunViolationException> {
                ConditionalMethods.loadClass(jar, "kotlin.Unit")
            }
            assertFailsWith<SafeRunViolationException> {
                ConditionalMethods.loadObject(jar, "kotlin.Unit")
            }
        } finally {
            LinearExecutor.executionOptions = original
        }
    }

    @Test
    fun `class loading reports missing archives`() {
        val missing = ConditionalMethods.loadClass(MPPPath("arara-not-there.jar"), "kotlin.Unit")
        assertEquals(ClassLoadingStatus.FILE_NOT_FOUND, missing.first)
        assertEquals(Any::class.java, missing.second)
        val viaString = ConditionalMethods.loadClass("arara-not-there.jar", "kotlin.Unit")
        assertEquals(ClassLoadingStatus.FILE_NOT_FOUND, viaString.first)
        val missingObject = ConditionalMethods.loadObject("arara-not-there.jar", "kotlin.Unit")
        assertEquals(ClassLoadingStatus.FILE_NOT_FOUND, missingObject.first)
    }

    @Test
    fun `class loading resolves and instantiates from an archive`() {
        val jar = classArchive(
            "kotlin/Unit.class",
            "org/islandoftex/arara/api/session/Command.class",
            "org/islandoftex/arara/core/rules/ConditionalMethodsTest.class",
        )
        val loaded = ConditionalMethods.loadClass(jar, "kotlin.Unit")
        assertEquals(ClassLoadingStatus.SUCCESS, loaded.first)
        assertEquals(kotlin.Unit::class.java, loaded.second)
        // the interface cannot be instantiated, and the singleton class
        // hides its constructor, so both report their own failure mode
        val notConstructible = ConditionalMethods.loadObject(
            jar, "org.islandoftex.arara.api.session.Command"
        )
        assertEquals(ClassLoadingStatus.INSTANTIATION_EXCEPTION, notConstructible.first)
        val hiddenConstructor = ConditionalMethods.loadObject(jar, "kotlin.Unit")
        assertEquals(ClassLoadingStatus.ILLEGAL_ACCESS, hiddenConstructor.first)
        val instantiated = ConditionalMethods.loadObject(
            jar, ConditionalMethodsTest::class.java.name
        )
        assertEquals(ClassLoadingStatus.SUCCESS, instantiated.first)
        assertTrue(instantiated.second is ConditionalMethodsTest)
    }

    /**
     * Builds a throwaway archive from class resources already on the
     * test classpath, so the loading helpers can resolve real classes
     * from a file of their own, without depending on any external
     * artifact.
     */
    private fun classArchive(vararg resources: String): MPPPath {
        val jar = Files.createTempFile("arara-classload-", ".jar")
        JarOutputStream(Files.newOutputStream(jar)).use { stream ->
            for (resource in resources) {
                val bytes = javaClass.classLoader.getResourceAsStream(resource)?.readBytes()
                    ?: error("the class resource $resource is not available")
                stream.putNextEntry(JarEntry(resource))
                stream.write(bytes)
                stream.closeEntry()
            }
        }
        return MPPPath(jar.toString())
    }
}

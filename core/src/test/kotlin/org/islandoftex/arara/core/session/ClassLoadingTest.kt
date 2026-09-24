// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.io.File
import java.nio.file.Files
import java.util.jar.JarOutputStream
import javax.tools.ToolProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.islandoftex.arara.api.SafeRunViolationException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.rules.ConditionalMethods
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Verifies the class loading resolution: lazily referenced companion
 * classes from the same archive resolve through the archive loader,
 * the user's file is copied rather than held open, and the safe-run
 * guard rejects the helpers.
 */
class ClassLoadingTest {
    /**
     * Compile a two-class fixture archive in the test: a main class
     * whose method body reaches a companion class from the same
     * archive, the exact shape lazy resolution must serve.
     */
    private fun fixtureArchive(): File {
        val workdir = Files.createTempDirectory("arara-classloading-")
        try {
            Files.writeString(
                workdir.resolve("Main.java"),
                "public class Main { " +
                    "public String reach() { return Helper.label(); } }"
            )
            Files.writeString(
                workdir.resolve("Helper.java"),
                "public class Helper { " +
                    "public static String label() { return \"companion\"; } }"
            )
            val compiler = ToolProvider.getSystemJavaCompiler()
            assertEquals(
                0,
                compiler.run(
                    null, null, null,
                    workdir.resolve("Main.java").toString(),
                    workdir.resolve("Helper.java").toString()
                ),
                "the fixture classes compile"
            )
            val archive = File.createTempFile("arara-fixture-", ".jar")
            JarOutputStream(archive.outputStream()).use { jar ->
                listOf("Main.class", "Helper.class").forEach { name ->
                    jar.putNextEntry(java.util.jar.JarEntry(name))
                    File(workdir.toFile(), name).inputStream().use {
                        it.copyTo(jar)
                    }
                    jar.closeEntry()
                }
            }
            return archive
        } finally {
            workdir.toFile().deleteRecursively()
        }
    }

    @Test
    fun `resolves companion classes from the same archive`() {
        val archive = fixtureArchive()
        val (status, loaded) = ClassLoading.loadClass(
            MPPPath(archive.toString()), "Main"
        )
        assertEquals(
            ClassLoading.ClassLoadingStatus.SUCCESS, status,
            "the fixture class loads"
        )
        // the invocation reaches Helper lazily through the archive
        // loader — the resolution this test exists for
        val instance = loaded.getDeclaredConstructor().newInstance()
        assertEquals(
            "companion",
            loaded.getMethod("reach").invoke(instance),
            "the companion class resolves through the archive loader"
        )
        archive.delete()
    }

    @Test
    fun `resolves companions when instantiated through the object helper`() {
        val archive = fixtureArchive()
        val (status, instance) = ClassLoading.loadObject(
            MPPPath(archive.toString()), "Main"
        )
        assertEquals(
            ClassLoading.ClassLoadingStatus.SUCCESS, status,
            "the fixture class loads and instantiates"
        )
        assertEquals(
            "companion",
            instance.javaClass.getMethod("reach").invoke(instance),
            "the companion resolves on an object provided by the helper"
        )
        archive.delete()
    }

    @Test
    fun `rejects loading classes in safe run mode`() {
        val options = LinearExecutor.executionOptions
        LinearExecutor.executionOptions = ExecutionOptions.from(options)
            .copy(executionMode = ExecutionMode.SAFE_RUN)
        try {
            val exception = assertFailsWith<SafeRunViolationException>(
                "safe mode must reject the class loading helpers"
            ) {
                ConditionalMethods.loadObject(
                    MPPPath("irrelevant.jar"), "com.example.Main"
                )
            }
            assertTrue(
                exception.message?.contains("safe mode") == true,
                "the violation names the safe mode contract"
            )
        } finally {
            LinearExecutor.executionOptions = options
        }
    }
}

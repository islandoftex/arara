// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath
import java.nio.file.Files
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import javax.tools.ToolProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ClassLoadingRegressionTest {
    private fun archive(root: MPPPath, version: String): MPPPath {
        val sources = root.resolve(version).apply { createDirectories() }
        val main = sources.resolve("Plugin.java").apply {
            writeText("public class Plugin { public String message() { return Companion.value(); } }")
        }
        val companion = sources.resolve("Companion.java").apply {
            writeText("public class Companion { public static String value() { return \"$version\"; } }")
        }
        val noDefault = sources.resolve("NoDefault.java").apply {
            writeText("public class NoDefault { public NoDefault(String value) {} }")
        }
        val compiler = assertNotNull(ToolProvider.getSystemJavaCompiler(), "The test requires a JDK")
        assertEquals(0, compiler.run(null, null, null, "-d", sources.toString(), main.toString(), companion.toString(), noDefault.toString()))
        val jar = root.resolve("plugin.jar")
        JarOutputStream(Files.newOutputStream(jar.toJVMPath())).use { output ->
            Files.newDirectoryStream(sources.toJVMPath(), "*.class").use { classes ->
                classes.forEach { file ->
                    output.putNextEntry(JarEntry(file.fileName.toString()))
                    Files.newInputStream(file).use { it.copyTo(output) }
                    output.closeEntry()
                }
            }
        }
        return jar
    }

    @Test
    fun replacingAnArchiveDoesNotChangeCompanionsOfAlreadyLoadedClasses() {
        val root = Files.createTempDirectory("arara-loader").toMPPPath()
        try {
            val jar = archive(root, "one")
            val first = ClassLoading.loadObject(jar, "Plugin")
            assertEquals(ClassLoading.ClassLoadingStatus.SUCCESS, first.first)
            archive(root, "two")
            assertEquals("one", first.second.javaClass.getMethod("message").invoke(first.second))
            val second = ClassLoading.loadObject(jar, "Plugin")
            assertEquals("two", second.second.javaClass.getMethod("message").invoke(second.second))
        } finally {
            ClassLoading.closeLoaders()
            root.removeDirectory()
        }
    }

    @Test
    fun missingDefaultConstructorIsReportedAsAnInstantiationFailure() {
        val root = Files.createTempDirectory("arara-constructor").toMPPPath()
        try {
            val jar = archive(root, "one")
            assertEquals(ClassLoading.ClassLoadingStatus.INSTANTIATION_EXCEPTION,
                         ClassLoading.loadObject(jar, "NoDefault").first)
        } finally {
            ClassLoading.closeLoaders()
            root.removeDirectory()
        }
    }
}

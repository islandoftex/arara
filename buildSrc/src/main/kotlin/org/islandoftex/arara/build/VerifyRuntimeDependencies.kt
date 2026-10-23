// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.io.DataInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/** Checks the bytecode selected by the minimum supported JVM, including multi-release dependencies. */
@CacheableTask
abstract class VerifyRuntimeDependencies : DefaultTask() {
    @get:Classpath
    abstract val dependencies: ConfigurableFileCollection

    @get:Input
    abstract val javaVersion: Property<Int>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val target = javaVersion.get()
        val failures = dependencies.files.sortedBy { it.name }.flatMap {
            runtimeBytecodeProblems(it.toPath(), target)
        }
        check(failures.isEmpty()) { failures.joinToString("\n", "Runtime dependency incompatibilities:\n") }
        val report = reportFile.get().asFile.toPath()
        Files.createDirectories(report.parent)
        Files.writeString(report, "Verified ${dependencies.files.size} dependency JARs for Java $target.\n")
        logger.lifecycle(Files.readString(report).trim())
    }
}

/** This checks class-file compatibility, not references to newer JDK APIs. */
internal fun runtimeBytecodeProblems(path: Path, javaVersion: Int): List<String> =
    JarFile(path.toFile(), false, JarFile.OPEN_READ, Runtime.Version.parse(javaVersion.toString())).use { jar ->
        jar.versionedStream().use { entries ->
            entries.filter { it.name.endsWith(".class") && !it.name.startsWith("META-INF/") }
                .sorted(compareBy { it.name })
                .map { entry ->
                    DataInputStream(jar.getInputStream(entry)).use {
                        classVersionProblem(it, "${path.fileName}: ${entry.name}", javaVersion)
                    }
                }.filter { it.isNotEmpty() }.toList()
        }
    }

private fun classVersionProblem(input: DataInputStream, location: String, javaVersion: Int): String {
    check(input.readInt() == CLASS_MAGIC) { "Invalid class in $location" }
    val minor = input.readUnsignedShort()
    val major = input.readUnsignedShort()
    return when {
        minor == PREVIEW_MINOR -> "$location requires preview features"
        major > javaVersion + CLASS_VERSION_OFFSET ->
            "$location requires Java ${major - CLASS_VERSION_OFFSET}"
        else -> ""
    }
}

private const val CLASS_MAGIC = -889275714
private const val CLASS_VERSION_OFFSET = 44
private const val PREVIEW_MINOR = 65535

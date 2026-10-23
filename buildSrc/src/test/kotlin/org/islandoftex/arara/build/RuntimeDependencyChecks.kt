// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

/** Synthetic JARs isolate class-version selection from the JDK running these checks. */
fun main(args: Array<String>) {
    val root = Files.createTempDirectory("arara-runtime-check")
    try {
        fun jar(name: String, multiRelease: Boolean = false, vararg classes: Triple<String, Int, Int>) =
            root.resolve("$name.jar").also { path ->
                val manifest = Manifest().apply {
                    mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
                    if (multiRelease) mainAttributes.putValue("Multi-Release", "true")
                }
                JarOutputStream(Files.newOutputStream(path), manifest).use { output ->
                    classes.forEach { (entry, major, minor) ->
                        output.putNextEntry(JarEntry(entry))
                        val bytes = ByteArrayOutputStream()
                        DataOutputStream(bytes).use {
                            it.writeInt(0xCAFEBABE.toInt())
                            it.writeShort(minor)
                            it.writeShort(major)
                        }
                        output.write(bytes.toByteArray())
                        output.closeEntry()
                    }
                }
            }
        val current = Triple("Example.class", 65, 0)
        val newer = Triple("Example.class", 69, 0)
        check(runtimeBytecodeProblems(jar("supported", false, current), 21).isEmpty())
        check(runtimeBytecodeProblems(jar("newer", false, newer), 21).single().contains("Java 25"))
        check(runtimeBytecodeProblems(jar("preview", false, Triple("Example.class", 65, 65535)), 21)
            .single().contains("preview"))
        val optional = jar("optional", true, current, Triple("META-INF/versions/25/Example.class", 69, 0))
        check(runtimeBytecodeProblems(optional, 21).isEmpty())
        check(runtimeBytecodeProblems(optional, 25).isEmpty())
        val selected = jar("selected", true, current, Triple("META-INF/versions/21/Example.class", 69, 0))
        check(runtimeBytecodeProblems(selected, 21).single().contains("Java 25"))
        val inactive = jar("inactive", false, current, Triple("META-INF/versions/21/Example.class", 69, 0))
        check(runtimeBytecodeProblems(inactive, 21).isEmpty())
        val replaced = jar("replaced", true, newer, Triple("META-INF/versions/21/Example.class", 65, 0))
        check(runtimeBytecodeProblems(replaced, 21).isEmpty())
        val versionOnly = jar("version-only", true, Triple("META-INF/versions/21/Extra.class", 69, 0))
        check(runtimeBytecodeProblems(versionOnly, 21).single().contains("Extra.class"))
        val broken = root.resolve("broken.jar")
        Files.writeString(broken, "invalid ZIP")
        check(runCatching { runtimeBytecodeProblems(broken, 21) }.isFailure)
        val fixture = root.resolve("fixture")
        Files.createDirectories(fixture)
        val dependency = fixture.resolve("dependency.jar")
        Files.copy(jar("fixture-supported", false, current), dependency)
        val classpath = args[1].split(System.getProperty("path.separator")).joinToString(", ") {
            "uri(\"${Paths.get(it).toUri()}\")"
        }
        Files.writeString(fixture.resolve("settings.gradle.kts"), "rootProject.name = \"runtime-check\"\n")
        Files.writeString(fixture.resolve("build.gradle.kts"), """
            buildscript { dependencies { classpath(files($classpath)) } }
            tasks.register<org.islandoftex.arara.build.VerifyRuntimeDependencies>("verifyRuntime") {
                dependencies.from(files("dependency.jar"))
                javaVersion.set(21)
                reportFile.set(layout.buildDirectory.file("runtime.txt"))
            }
        """.trimIndent())
        fun runner() = GradleRunner.create()
            .withGradleInstallation(Paths.get(args[0]).toFile())
            .withProjectDir(fixture.toFile())
            .withTestKitDir(root.resolve("test-kit").toFile())
            .withArguments("verifyRuntime", "--stacktrace", "--build-cache")
        check(runner().build().task(":verifyRuntime")?.outcome == TaskOutcome.SUCCESS)
        check(runner().build().task(":verifyRuntime")?.outcome == TaskOutcome.UP_TO_DATE)
        Files.delete(fixture.resolve("build/runtime.txt"))
        check(runner().build().task(":verifyRuntime")?.outcome == TaskOutcome.FROM_CACHE)
        Files.copy(jar("fixture-newer", false, newer), dependency, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        check(runner().buildAndFail().output.contains("Example.class requires Java 25"))
        println("Runtime dependency bytecode and Gradle task checks passed.")
    } finally {
        Files.walk(root).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach(Files::delete) }
    }
}

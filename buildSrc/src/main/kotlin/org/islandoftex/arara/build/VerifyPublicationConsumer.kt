// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.nio.file.Files
import java.util.Comparator
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.testkit.runner.GradleRunner
import org.gradle.work.DisableCachingByDefault

/** Builds a separate Gradle application against freshly staged Maven artifacts. */
@DisableCachingByDefault(because = "Exercises dependency resolution against freshly staged publications")
abstract class VerifyPublicationConsumer : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repositoryDirectory: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val fixtureDirectory: DirectoryProperty

    @get:Input
    abstract val publicationVersion: Property<String>

    @get:Input
    abstract val kotlinVersion: Property<String>

    @get:LocalState
    abstract val workingDirectory: DirectoryProperty

    @get:LocalState
    abstract val testKitDirectory: DirectoryProperty

    @get:Internal
    abstract val gradleInstallation: DirectoryProperty

    @get:Internal
    abstract val gradleUserHome: DirectoryProperty

    @get:Internal
    abstract val consumerJavaHome: DirectoryProperty

    @TaskAction
    fun verify() {
        val working = workingDirectory.get().asFile.toPath()
        if (Files.exists(working)) {
            Files.walk(working).use { paths ->
                paths.sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
            }
        }
        val fixture = fixtureDirectory.get().asFile.toPath()
        Files.walk(fixture).use { paths ->
            paths.forEach { source ->
                val destination = working.resolve(fixture.relativize(source))
                if (Files.isDirectory(source)) Files.createDirectories(destination)
                else Files.copy(source, destination)
            }
        }
        // TestKit requires File at its API boundary; staging uses NIO paths.
        GradleRunner.create()
            .withGradleInstallation(gradleInstallation.get().asFile)
            .withProjectDir(working.toFile())
            .withTestKitDir(testKitDirectory.get().asFile)
            .withArguments(
                "run", "--stacktrace", "--no-configuration-cache",
                "--gradle-user-home", gradleUserHome.get().asFile.absolutePath,
                "-Dorg.gradle.java.home=${System.getProperty("java.home")}",
                "-Porg.gradle.java.installations.paths=${consumerJavaHome.get().asFile.absolutePath}",
                "-Porg.gradle.java.installations.auto-detect=false",
                "-Porg.gradle.java.installations.auto-download=false",
                "-PpublicationRepository=${repositoryDirectory.get().asFile.toURI()}",
                "-PpublicationVersion=${publicationVersion.get()}",
                "-PconsumerKotlinVersion=${kotlinVersion.get()}"
            )
            .forwardOutput()
            .build()
    }
}

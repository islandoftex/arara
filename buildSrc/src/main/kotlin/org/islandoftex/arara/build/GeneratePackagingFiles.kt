// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Generates the launcher and man page from declared version and date inputs. */
@CacheableTask
abstract class GeneratePackagingFiles : DefaultTask() {
    @get:Input
    abstract val packageVersion: Property<String>

    @get:Input
    abstract val manPageDate: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val directory = outputDirectory.get().asFile.toPath()
        Files.createDirectories(directory)
        TaskHelper.createScript(directory.resolve("arara.sh"))
        val date = LocalDate.parse(manPageDate.get()).format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH))
        TaskHelper.createManPage(directory.resolve("arara.1"), packageVersion.get(), date)
    }
}

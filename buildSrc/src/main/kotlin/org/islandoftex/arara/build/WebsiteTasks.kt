// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.islandoftex.arara.build.website.WebsiteBooks
import org.islandoftex.arara.build.website.WebsiteContent
import org.islandoftex.arara.build.website.WebsiteSources
import javax.inject.Inject

abstract class PrepareWebsiteTask : DefaultTask() {
    @get:Internal
    abstract val websiteDirectory: DirectoryProperty
    @get:InputDirectory @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val themeDirectory: DirectoryProperty
    @get:InputFile @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val readme: RegularFileProperty
    @get:InputFile @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val changelog: RegularFileProperty
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    init {
        // Rendered output and the submodule are tracked separately from site sources.
        inputs.files(websiteDirectory.asFileTree.matching { exclude("public/**", "themes/**") })
    }

    @TaskAction fun prepare() {
        val output = outputDirectory.get().asFile
        if (output.exists()) check(output.deleteRecursively()) { "Cannot clear staged website: $output" }
        WebsiteSources.prepare(readme.get().asFile.parentFile, output, themeDirectory.get().asFile)
    }
}

abstract class CheckWebsiteTask : DefaultTask() {
    @get:InputDirectory @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val siteDirectory: DirectoryProperty
    @get:Input abstract val siteUrl: Property<String>
    @TaskAction fun checkSite() = WebsiteContent.checkSite(siteDirectory.get().asFile, siteUrl.get())
}

abstract class BuildWebsiteBookTask @Inject constructor(private val execOperations: ExecOperations) : DefaultTask() {
    @get:InputDirectory @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val siteDirectory: DirectoryProperty
    @get:InputDirectory @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val resourcesDirectory: DirectoryProperty
    @get:Input abstract val manual: Property<Boolean>
    @get:Input abstract val bookVersion: Property<String>
    @get:Input abstract val siteUrl: Property<String>
    @get:Input abstract val converter: Property<String>
    @get:OutputFile abstract val htmlFile: RegularFileProperty
    @get:OutputFile abstract val pdfFile: RegularFileProperty

    init { converter.convention("weasyprint") }

    @TaskAction fun buildBook() {
        val site = siteDirectory.get().asFile
        val html = htmlFile.get().asFile
        WebsiteBooks(site, resourcesDirectory.get().asFile, siteUrl.get())
            .assemble(html, manual.get(), bookVersion.get())
        val pdf = pdfFile.get().asFile
        pdf.parentFile.mkdirs()
        execOperations.exec { commandLine(converter.get(), "-u", site.toURI().toASCIIString(), html, pdf) }
    }
}

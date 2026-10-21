// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import java.time.LocalDate
import java.time.ZoneOffset

/** Stages one TDS tree and derives the CTAN layout with native Gradle tasks. */
class AraraPackaging : Plugin<Project> {
    override fun apply(target: Project) {
        val version = target.version.toString()
        val generated = target.tasks.register("generatePackagingFiles", GeneratePackagingFiles::class.java) {
            packageVersion.set(version)
            manPageDate.set(target.providers.gradleProperty("packagingDate")
                .orElse(target.provider { LocalDate.now(ZoneOffset.UTC).toString() }))
            outputDirectory.set(target.layout.buildDirectory.dir("packaging/generated"))
        }
        val sources = target.tasks.register("assembleCTANSourceZip", Zip::class.java) {
            configureArchive(this, target, "arara-$version-src.zip")
            from(target.projectDir) {
                include("api/**", "core/**", "lua/**", "mvel/**", "cli/**", "docs/**", "smoke-tests/**")
                include("buildSrc/**", "gradle/**", "website/**", "rules/**")
                include("*.gradle.kts", "gradle.properties", "gradlew", "gradlew.bat")
                include("README.md", "CHANGELOG.md", "LICENSE", "detekt-config.yml", ".editorconfig")
                include("flake.nix", "flake.lock", ".gitmodules", ".gitattributes", ".gitignore")
                exclude("*/build/**", "build/**", "**/.gradle/**", "**/.git", "**/.git/**", "website/public/**")
                exclude("docs/*.pdf", "docs/resources/version.txt", "**/arara.log")
                exclude("api/src/jvmMain/kotlin/org/islandoftex/arara/api/AraraAPI.kt")
            }
            filesMatching("gradlew") { permissions { unix("0755") } }
        }
        val website = target.files(target.layout.projectDirectory.dir("website/public"))
            .builtBy(":docs:buildWebsite")
        val documentationSources = target.tasks.register("assembleDocumentationSourceZip", Zip::class.java) {
            configureArchive(this, target, "arara-$version-docsrc.zip")
            from(website)
        }
        val books = target.files(
            target.layout.projectDirectory.file("docs/arara-manual.pdf"),
            target.layout.projectDirectory.file("docs/arara-quickstart.pdf")
        ).builtBy(":docs:buildManual", ":docs:buildQuickstartGuide")
        val application = target.files(target.provider {
            target.project(":cli").tasks.named("shadowJar", Jar::class.java).get().archiveFile.get()
        }).builtBy(":cli:shadowJar")
        val tds = target.tasks.register("assembleTDSTree", Sync::class.java) {
            group = "distribution"
            duplicatesStrategy = DuplicatesStrategy.FAIL
            includeEmptyDirs = false
            into(target.layout.buildDirectory.dir("tds"))
            into("doc/support/arara") {
                from(books)
                from(documentationSources.flatMap { it.archiveFile })
                from(target.layout.projectDirectory.file("README.md")) {
                    filter { it.replace("# arara", "# arara v$version") }
                }
            }
            into("doc/man/man1") { from(generated.flatMap { it.outputDirectory.file("arara.1") }) }
            into("scripts/arara") {
                from(application) { rename { "arara.jar" } }
                from(generated.flatMap { it.outputDirectory.file("arara.sh") })
                into("rules") { from(target.layout.projectDirectory.dir("rules")) }
            }
            into("source/support/arara") { from(sources.flatMap { it.archiveFile }) }
            filesMatching("**/arara.sh") { permissions { unix("0755") } }
        }
        val tdsZip = target.tasks.register("assembleTDSZip", Zip::class.java) {
            configureArchive(this, target, "arara.tds.zip")
            from(tds)
            filesMatching("**/arara.sh") { permissions { unix("0755") } }
        }
        val ctan = target.tasks.register("assembleCTANTree", Sync::class.java) {
            group = "distribution"
            duplicatesStrategy = DuplicatesStrategy.FAIL
            includeEmptyDirs = false
            into(target.layout.buildDirectory.dir("ctan"))
            from(tdsZip.flatMap { it.archiveFile })
            into("arara") {
                from(tds.map { it.destinationDir.resolve("doc/support/arara/README.md") })
                into("doc") {
                    from(tds.map { it.destinationDir.resolve("doc/support/arara") }) { exclude("README.md") }
                    from(tds.map { it.destinationDir.resolve("doc/man/man1/arara.1") })
                }
                into("scripts") { from(tds.map { it.destinationDir.resolve("scripts/arara") }) }
                into("source") { from(tds.map { it.destinationDir.resolve("source/support/arara") }) }
            }
            filesMatching("**/arara.sh") { permissions { unix("0755") } }
        }
        target.tasks.register("assembleCTAN", Zip::class.java) {
            configureArchive(this, target, "arara-ctan.zip")
            from(ctan)
            filesMatching("**/arara.sh") { permissions { unix("0755") } }
        }
    }

    private fun configureArchive(task: Zip, target: Project, name: String) {
        task.group = "distribution"
        task.destinationDirectory.set(target.layout.buildDirectory)
        task.archiveFileName.set(name)
        task.duplicatesStrategy = DuplicatesStrategy.FAIL
        task.includeEmptyDirs = false
        task.isPreserveFileTimestamps = false
        task.isReproducibleFileOrder = true
    }
}

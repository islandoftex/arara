// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermissions
import java.util.zip.ZipFile
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

private val packagingTasks = listOf("generatePackagingFiles", "assembleCTANSourceZip",
    "assembleDocumentationSourceZip", "assembleTDSTree", "assembleTDSZip", "assembleCTANTree", "assembleCTAN")

/** Exercises real Gradle staging/archives with small inputs and no TeX or application compilation. */
fun main(args: Array<String>) {
    val root = Files.createTempDirectory("arara packaging ")
    try {
        fun write(name: String, text: String) {
            val path = root.resolve(name)
            Files.createDirectories(path.parent)
            Files.writeString(path, text)
        }
        val classpath = args[1].split(System.getProperty("path.separator")).joinToString(", ") {
            "uri(\"${Paths.get(it).toUri()}\")"
        }
        write("settings.gradle.kts", "rootProject.name = \"arara\"\ninclude(\"cli\", \"docs\")\n")
        write("build.gradle.kts", """
            buildscript { dependencies { classpath(files($classpath)) } }
            plugins { base }
            version = "1.2.3"
            apply<org.islandoftex.arara.build.AraraPackaging>()
        """.trimIndent())
        write("cli/build.gradle.kts", """
            tasks.register<Sync>("stageNativeRuntime") {
                from("application.txt") { rename { "jna.jar" } }
                into(layout.buildDirectory.dir("native"))
            }
            tasks.register<Jar>("shadowJar") {
                from("application.txt")
                archiveFileName.set("application.jar")
                destinationDirectory.set(layout.buildDirectory.dir("libs"))
            }
        """.trimIndent())
        write("cli/application.txt", "application")
        write("docs/build.gradle.kts", """
            tasks.register<Sync>("buildWebsite") {
                from("site")
                into(rootProject.layout.projectDirectory.dir("website/public"))
            }
            listOf("buildManual" to "arara-manual.pdf", "buildQuickstartGuide" to "arara-quickstart.pdf")
                .forEach { (taskName, filename) ->
                    val output = layout.projectDirectory.file(filename)
                    tasks.register(taskName) {
                        outputs.file(output)
                        doLast { output.asFile.writeText("%PDF-fixture") }
                    }
                }
        """.trimIndent())
        write("docs/site/index.html", "website")
        write("README.md", "# arara\n")
        write("gradlew", "wrapper")
        write("gradlew.bat", "wrapper")
        write("gradle/wrapper/gradle-wrapper.jar", "wrapper")
        write("gradle/libs.versions.toml", "[versions]\n")
        write("buildSrc/build.gradle.kts", "plugins { base }\n")
        write("buildSrc/src/main/kotlin/org/islandoftex/arara/build/Fixture.kt", "// build logic\n")
        listOf("api", "core", "lua", "mvel", "smoke-tests").forEach { module ->
            write("$module/build.gradle.kts", "// module\n")
            write("$module/src/commonMain/kotlin/Fixture.kt", "// source\n")
            write("$module/build/leak.txt", "must not ship")
        }
        write("api/src/jvmMain/kotlin/org/islandoftex/arara/api/AraraAPI.kt", "// generated\n")
        write("rules/arara-rule-probe.yaml", "rule")
        write("rules/arara-rule-probe.lua", "rule")
        write("website/config.toml", "title = 'fixture'\n")
        val installation = Paths.get(args[0]).toFile()
        fun build(vararg tasks: String) = GradleRunner.create().withGradleInstallation(installation)
            .withProjectDir(root.toFile()).withArguments(tasks.toList() +
                listOf("-PpackagingDate=2020-02-03", "--stacktrace")).build()
        build("assembleCTAN")
        val archives = listOf("arara-1.2.3-src.zip", "arara-1.2.3-docsrc.zip", "arara.tds.zip", "arara-ctan.zip")
        fun entries(name: String) = ZipFile(root.resolve("build/$name").toFile()).use { zip ->
            val names = zip.entries().asSequence().filterNot { it.isDirectory }.map { it.name }.toList()
            check(names.size == names.toSet().size) { "Duplicate entries in $name" }
            names.toSet()
        }
        val sources = entries(archives[0])
        listOf("mvel/build.gradle.kts", "buildSrc/src/main/kotlin/org/islandoftex/arara/build/Fixture.kt",
            "settings.gradle.kts", "gradle/wrapper/gradle-wrapper.jar", "gradlew.bat",
            "rules/arara-rule-probe.lua", "website/config.toml").forEach { check(it in sources) { it } }
        check(sources.none { it.endsWith("leak.txt") || it.endsWith("/AraraAPI.kt") ||
            it.startsWith("website/public/") || it == "docs/arara-manual.pdf" })
        val tds = entries(archives[2])
        listOf("doc/support/arara/arara-manual.pdf", "doc/support/arara/arara-quickstart.pdf",
            "doc/man/man1/arara.1", "scripts/arara/arara.jar", "scripts/arara/jna.jar", "scripts/arara/rules/arara-rule-probe.lua",
            "source/support/arara/arara-1.2.3-src.zip").forEach { check(it in tds) { it } }
        check(tds.none { "/build/" in it || "/site/" in it })
        listOf(archives[0] to "gradlew", archives[2] to "scripts/arara/arara.sh",
            archives[3] to "arara/scripts/arara.sh").forEach { (archive, name) ->
            FileSystems.newFileSystem(root.resolve("build/$archive"),
                mapOf("enablePosixFileAttributes" to true, "defaultPermissions" to "r--------")).use { zip ->
                check(PosixFilePermissions.toString(Files.getPosixFilePermissions(zip.getPath("/$name"))) ==
                    "rwxr-xr-x") {
                    "Missing executable permission: $archive:$name"
                }
            }
        }
        ZipFile(root.resolve("build/arara.tds.zip").toFile()).use { zip ->
            val script = zip.getInputStream(zip.getEntry("scripts/arara/arara.sh"))
                .bufferedReader().use { it.readText() }
            check("--add-modules=com.sun.jna" in script)
            check("--enable-native-access=com.sun.jna" in script)
            check("ALL-UNNAMED" !in script)
        }
        val ctan = entries(archives[3])
        check(setOf("arara.tds.zip", "arara/README.md", "arara/doc/arara.1", "arara/scripts/arara.sh", "arara/scripts/jna.jar")
            .all { it in ctan })
        check("arara/doc/README.md" !in ctan)
        check(Files.readString(root.resolve("build/tds/doc/man/man1/arara.1")).contains("03 February 2020"))
        val second = build("assembleCTAN")
        packagingTasks.forEach { check(second.task(":$it")?.outcome == TaskOutcome.UP_TO_DATE) { it } }
        val original = archives.associateWith { Files.readAllBytes(root.resolve("build/$it")) }
        Files.setLastModifiedTime(root.resolve("README.md"), FileTime.fromMillis(1))
        build("clean", "assembleCTAN")
        archives.forEach { check(original.getValue(it).contentEquals(Files.readAllBytes(root.resolve("build/$it")))) {
            "Non-reproducible archive: $it"
        } }
        write("rules/transient.yaml", "temporary")
        build("assembleCTAN")
        check("scripts/arara/rules/transient.yaml" in entries(archives[2]))
        Files.delete(root.resolve("rules/transient.yaml"))
        build("assembleCTAN")
        check(entries(archives[2]).none { it.endsWith("transient.yaml") })
        check(entries(archives[3]).none { it.endsWith("transient.yaml") })
        fun failure() = GradleRunner.create().withGradleInstallation(installation)
            .withProjectDir(root.toFile()).withArguments("assembleCTANSourceZip").buildAndFail().output
        write("website/static/unfetched.ttf", "version https://git-lfs.github.com/spec/v1\noid sha256:fake\n")
        check("Unresolved Git LFS pointer" in failure())
        Files.delete(root.resolve("website/static/unfetched.ttf"))
        write("duplicate/README.md", "duplicate")
        Files.writeString(root.resolve("build.gradle.kts"),
            "\ntasks.named<Zip>(\"assembleCTANSourceZip\") { from(\"duplicate\") }\n",
            java.nio.file.StandardOpenOption.APPEND)
        check("README.md" in failure())
        println("Packaging layout, complete sources, duplicate exclusion, incremental and reproducibility checks passed.")
    } finally {
        Files.walk(root).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach { Files.delete(it) } }
    }
}

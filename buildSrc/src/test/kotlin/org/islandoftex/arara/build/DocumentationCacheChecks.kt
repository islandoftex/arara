// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

/** Verifies PDF reuse across CI checkouts and invalidation without invoking WeasyPrint. */
fun main(args: Array<String>) {
    if (System.getProperty("os.name").startsWith("Windows")) return
    val root = Files.createTempDirectory("arara documentation cache ")
    try {
        val converter = root.resolve("converter")
        val counter = root.resolve("renders.txt")
        Files.writeString(converter, "#!/bin/sh\nprintf 'render\\n' >> '$counter'\nprintf '%%PDF-fixture\\n' > \"\$4\"\n")
        Files.setPosixFilePermissions(converter, PosixFilePermissions.fromString("rwx------"))
        val classpath = args[1].split(System.getProperty("path.separator")).joinToString(", ") {
            "uri(\"${Paths.get(it).toUri()}\")"
        }
        fun fixture(name: String): Path {
            val directory = root.resolve(name)
            Files.createDirectories(directory.resolve("site/quickstart"))
            Files.createDirectories(directory.resolve("resources"))
            Files.writeString(directory.resolve("site/quickstart/index.html"),
                "<h1 data-arara-document-title>Quickstart</h1><div data-arara-document-content>Text</div>")
            Files.writeString(directory.resolve("resources/manual.css"), "body { color: black; }")
            Files.writeString(directory.resolve("flake.lock"), "pinned tools")
            Files.writeString(directory.resolve("settings.gradle.kts"), """
                rootProject.name = "docs-fixture"
                buildCache { local { directory = file("${root.resolve("cache")}") } }
            """.trimIndent())
            Files.writeString(directory.resolve("build.gradle.kts"), """
                buildscript { dependencies { classpath(files($classpath)) } }
                tasks.register<org.islandoftex.arara.build.BuildWebsiteBookTask>("book") {
                    siteDirectory.set(layout.projectDirectory.dir("site"))
                    resourcesDirectory.set(layout.projectDirectory.dir("resources"))
                    toolchainLock.set(layout.projectDirectory.file("flake.lock"))
                    manual.set(false)
                    bookVersion.set(providers.gradleProperty("bookVersion").orElse("1.2.3"))
                    siteUrl.set("https://example.invalid")
                    converter.set("$converter")
                    converterVersion.set(providers.gradleProperty("converterVersion").orElse("fixture-1"))
                    htmlFile.set(layout.buildDirectory.file("book.html"))
                    pdfFile.set(layout.buildDirectory.file("book.pdf"))
                }
            """.trimIndent())
            return directory
        }
        fun run(directory: Path, vararg options: String) = GradleRunner.create()
            .withGradleInstallation(Paths.get(args[0]).toFile()).withProjectDir(directory.toFile())
            .withArguments(listOf("book", "--build-cache", "--stacktrace") + options).build()
            .task(":book")!!.outcome
        val first = fixture("first")
        check(run(first) == TaskOutcome.SUCCESS)
        val second = fixture("relocated")
        check(run(second) == TaskOutcome.FROM_CACHE)
        check(Files.readString(second.resolve("build/book.pdf")).startsWith("%PDF-"))
        check(!Files.exists(second.resolve("build/book.html"))) { "Checkout-specific HTML must not be cached" }
        check(Files.readAllLines(counter).size == 1) { "The relocated checkout reran the converter" }
        check(run(second, "-PbookVersion=1.2.3-SNAPSHOT") == TaskOutcome.SUCCESS)
        check(run(second, "-PbookVersion=1.2.3-SNAPSHOT", "-PconverterVersion=fixture-2") == TaskOutcome.SUCCESS)
        Files.writeString(second.resolve("flake.lock"), "updated tools")
        check(run(second) == TaskOutcome.SUCCESS)
        Files.writeString(second.resolve("resources/manual.css"), "body { color: blue; }")
        check(run(second) == TaskOutcome.SUCCESS)
        Files.writeString(second.resolve("site/quickstart/index.html"),
            "<h1 data-arara-document-title>Quickstart</h1><div data-arara-document-content>Updated</div>")
        check(run(second) == TaskOutcome.SUCCESS)
        check(Files.readAllLines(counter).size == 6)
        println("PASS relocated documentation cache and rendering-input invalidation")
    } finally {
        root.toFile().deleteRecursively()
    }
}

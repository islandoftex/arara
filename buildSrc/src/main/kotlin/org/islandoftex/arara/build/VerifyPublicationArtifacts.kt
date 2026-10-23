// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import java.io.DataInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.w3c.dom.Element

private const val JAVA_21_CLASS_VERSION = 65

/** Checks the artifacts actually written to the isolated Maven repository. */
@DisableCachingByDefault(because = "Verifies freshly staged Maven publications")
abstract class VerifyPublicationArtifacts : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val repositoryDirectory: DirectoryProperty

    @get:Input
    abstract val moduleNames: ListProperty<String>

    @get:Input
    abstract val publicationVersion: Property<String>

    @TaskAction
    fun verify() {
        val group = repositoryDirectory.get().asFile.toPath().resolve("org/islandoftex/arara")
        moduleNames.get().forEach { module ->
            listOf("arara-$module", "arara-$module-jvm").forEach { artifact ->
                verifyArtifact(group, module, artifact)
            }
        }
        logger.lifecycle("Verified {} KMP/JVM publication pairs.", moduleNames.get().size)
    }

    private fun verifyArtifact(group: Path, module: String, artifact: String) {
        val directory = group.resolve(artifact).resolve(publicationVersion.get())
        val files = Files.list(directory).use { it.toList() }
        fun file(suffix: String): Path = files.singleOrNull { it.fileName.toString().endsWith(suffix) }
            ?: error("Expected one $suffix artifact in $directory")
        val pom = readPom(file(".pom"))
        check(pom.text("artifactId") == artifact) { "Wrong artifact ID in $directory" }
        check(pom.text("groupId") == "org.islandoftex.arara") { "Wrong group in $directory" }
        check(pom.text("version") == publicationVersion.get()) { "Wrong version in $directory" }
        check(pom.text("name").isNotBlank()) { "Missing POM name in $directory" }
        file(".module")
        withJar(file("-javadoc.jar")) { jar ->
            check(jar.getJarEntry("index.html") != null) { "Empty API reference for $artifact" }
        }
        withJar(file("-sources.jar")) { jar ->
            val sources = jar.entries().asSequence().map { it.name }.filter { it.endsWith(".kt") }.toList()
            check(sources.isNotEmpty()) { "Empty Kotlin sources for $artifact" }
            check(sources.none { it.startsWith("commonTest/") || it.startsWith("jvmTest/") }) {
                "Published test sources: $artifact"
            }
            if (artifact == "arara-api-jvm") {
                check(sources.any { it.endsWith("/AraraAPI.kt") }) { "Generated API version source is missing" }
            }
        }
        if (artifact.endsWith("-jvm")) {
            val binary = files.single {
                val name = it.fileName.toString()
                name.endsWith(".jar") && !name.endsWith("-sources.jar") && !name.endsWith("-javadoc.jar")
            }
            withJar(binary) { jar ->
                check(jar.manifest.mainAttributes.getValue("Automatic-Module-Name") ==
                    "org.islandoftex.arara.$module") { "Wrong JVM module name for $artifact" }
                val classes = jar.entries().asSequence().filter { it.name.endsWith(".class") }.toList()
                check(classes.isNotEmpty()) { "Empty JVM library: $artifact" }
                classes.filterNot { it.name.startsWith("META-INF/versions/") }.forEach { entry ->
                    DataInputStream(jar.getInputStream(entry)).use { input ->
                        input.readInt() // magic
                        input.readUnsignedShort() // minor version
                        val major = input.readUnsignedShort()
                        check(major <= JAVA_21_CLASS_VERSION) {
                            "${entry.name} requires a JVM newer than Java 21"
                        }
                    }
                }
            }
        }
        if (artifact == "arara-core-jvm") {
            val dependencies = pom.getElementsByTagName("dependency")
            check((0 until dependencies.length).map { dependencies.item(it) as Element }.any {
                it.text("artifactId") == "arara-api-jvm" && it.text("scope") == "compile"
            }) { "Core must expose API as a Maven compile dependency" }
        }
    }

    private fun readPom(path: Path): Element {
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        return Files.newInputStream(path).use { factory.newDocumentBuilder().parse(it).documentElement }
    }

    private fun Element.text(name: String): String = childNodes.let { nodes ->
        (0 until nodes.length).map { nodes.item(it) }.filterIsInstance<Element>()
            .firstOrNull { it.tagName == name }?.textContent.orEmpty()
    }

    // JarFile's API requires File; all other filesystem operations use Path.
    private fun withJar(path: Path, action: (JarFile) -> Unit) = JarFile(path.toFile()).use(action)
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.islandoftex.roastmyjar.core.JarFile
import org.islandoftex.roastmyjar.report.Report
import org.islandoftex.roastmyjar.util.SearchUtils
import kotlin.io.path.div
import kotlin.io.path.isDirectory

/**
 * Obtains the highest bytecode version from the built Java archive artifact
 * (should be `arara-cli-with-deps-*.jar`) and checks it against the project's
 * JVM target (should be available in `gradle.properties` in the root directory
 * under the`arara.jvm.target` key). If both values match, the project was
 * roasted successfully (and no exception is thrown).
 */
open class  RoastTask : DefaultTask() {
    init {
        group = "check"
        description = "Checks whether the highest bytecode version matches the project's JVM target."
    }

    @TaskAction
    fun run() {
        val searchPath = project.rootDir.toPath() / "cli/build/libs"
        logger.info("Searching for the proper binary in: $searchPath")
        SearchUtils.findFirst(searchPath, "arara-cli-with-deps-*.jar")
                ?.let { path ->

                    logger.info("Found archive file: $path")
                    val jar = JarFile.fromPath(path)

                    logger.info("Trying to get the project's JVM target from: gradle.properties")
                    val expected = runCatching {
                        project
                            .file("gradle.properties")
                            .readLines()
                            .first { it.startsWith("arara.jvm.target") }
                            .substringAfter("=")
                            .trim()
                        }
                        .getOrDefault("11")

                    logger.info("Expected Java version: $expected")

                    logger.info("Trying to get the highest version from the archive.")
                    val actual = jar
                            .entries
                            .mapNotNull { it.version }
                            .maxOrNull()
                            ?.toJavaVersion()

                    if (actual == null) {
                        logger.info("Something wrong happened with the archive generation.")
                        throw GradleException("Archive has no valid classes.")
                    }
                    else {
                        logger.info("Generating report.")
                        Report.forJarFile(jar).print()

                        if (actual != expected) {
                            logger.info("Bytecode mismatch, something wrong happened.")
                            throw GradleException("Bytecode mismatch (expected $expected, found $actual).")
                        }
                        else {
                            "Project roasted successfully!".let {
                                println(it)
                                logger.info(it)
                            }
                        }
                    }
                } ?: throw GradleException("Archive file not found.")
    }
}
